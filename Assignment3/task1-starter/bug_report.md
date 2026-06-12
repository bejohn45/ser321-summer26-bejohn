# StringConcatenation Debugging Exercise

## Overview
The stringconcatenation service is implemented in both client and server, but has **4 bugs** that prevent it from working correctly according to the protocol specification.

The Correct Protocol is in the README.md

---

## The 4 Bugs

### Bug #1:  Client sends field str1 instead of string1 
**Location:** `SockClient.java`, line 74

**The Problem:**
```
json.put("str1", str1);

The client sends the first string with field name str1, but the protocol needs it to be string1.

Request:

    {
        "type" : "stringconcatenation",
        "string1" : <String>, -- first string
        "string2" : <String> -- second string
    }
```

**The Fix:**
```
json.put("string1", str1);
```

**Why it matters:** 
The server checks for the client sending string1. If the client sends str1, the server will not see it coming in and think the client didn't send the field.

**How did you find this:**
I saw the second string being sent by the client usese string2, but the first is sending str1. After checking the protocol I found that the first string was being sent incorrectly.

### Bug #2:  Server does not check for string2 field
**Location:** `SockServer.java`, line 187

**The Problem:**
```
The server tries to read string2 without checking if it exists.
```

**The Fix:**
```
JSONObject res = testField(req, "string1");
    if (!res.getBoolean("ok")) {
      return res;
    }
res = testField(req, "string2");
    if (!res.getBoolean("ok")) {
      return res;
    }
```

**Why it matters:** 
The protocol needs to have 2 strings to concatenate. If string2 is not provided by the client, the server may crash when trying to read it.

**How did you find this:**
I saw that the testField method was called on string1, but not on string2. The protocol shows both string1 and string2 are required, so both fields would need to be checked.

### Bug #3:  Server returns wrong response type as concat
**Location:** `SockServer.java`, line 184

**The Problem:**
```
res.put("type", "concat");

The server returns "type" : "concat", but the protocol needs the type to be "stringconcatenation"

Success response:

    {
        "type" : "stringconcatenation",
        "ok" : true,
        "result" : <String> -- concatenated string
    }
```

**The Fix:**
```
res.put("type", "stringconcatenation");
```

**Why it matters:** 
The response type should match the protocol. If the server returns "concat", the client receiving response may not know it is for the string concatenation request they sent.

**How did you find this:**
I saw the protocol showing the type for request and response should be "stringconcatenation", but the server was responding with "concat".

### Bug #4:  Server returns wrong result field as combined
**Location:** `SockServer.java`, line 188

**The Problem:**
```
res.put("combined", str1 + str2);

The server returns concatenated string with field name "combined", but the protocol needs it to be called "result".

Success response:

    {
        "type" : "stringconcatenation",
        "ok" : true,
        "result" : <String> -- concatenated string
    }
```

**The Fix:**
```
res.put("result", str1 + str2);
```

**Why it matters:** 
The client is looking for "result" as a field returned from the server, but the server is sending it as "combined".

**How did you find this:**
I saw the protocol shows the response for the concatenated string to be in "result", but server is responding with "combined".

