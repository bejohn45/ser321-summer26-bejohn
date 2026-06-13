# Task 1.2: Mystery Service Discovery and Protocol Documentation

**Your Name:** Bevin John
**How I tested:** Unit Tests in Testing.java

---

## Part 1: Discovery Log

Document at least 8 test attempts showing your systematic investigation.

### Attempt 1
**Request Sent:**
```json
{
    "type" : "analyzer"
}
```

**Response Received:**
```json
{
    "ok" : false,
    "message" : "Field 'action' does not exist in request. Hint: what action do you want to perform?"
}
```

**What I Learned:**
The service needs a field ‘action’.

---

### Attempt 2
**Request Sent:**
```json
{
    "type" : "analyzer",
    "action" : "repeat"
}
```

**Response Received:**
```json
{
    "ok" : false,
    "message" : "Field 'text' does not exist in request"
}
```

**What I Learned:**
The service also needs a field 'text'.

---

### Attempt 3
**Request Sent:**
```json
{
    "type" : "analyzer",
    "action" : "repeat",
    "text" : "hello"
}
```

**Response Received:**
```json
{
    "ok" : false,
    "message" : "Action 'repeat' not supported. Valid actions: wordcount, charcount, search"
}
```

**What I Learned:**
Valid actions are wordcount, charcount, or search.

---

### Attempt 4
**Request Sent:**
```json
{
    "type" : "analyzer",
    "action" : "wordcount",
    "text" : "hello"
}
```

**Response Received:**
```json
{
    "count" : 1,
    "action" : "wordcount",
    "type" : "analyzer",
    "ok" : true
}
```

**What I Learned:**
The ‘action’ wordcount looks to count words with given ‘text’.

---

### Attempt 5
**Request Sent:**
```json
{
    "type" : "analyzer",
    "action" : "wordcount",
    "text" : "hello my name is bevin"
}
```

**Response Received:**
```json
{
    "count" : 5,
    "action" : "wordcount",
    "type" : "analyzer",
    "ok" : true
}
```

**What I Learned:**
The ‘action’ wordcount definitely counts words with given ‘text’.

---

### Attempt 6
**Request Sent:**
```json
{
    "type" : "analyzer",
    "action" : "charcount",
    "text" : "hello my name is bevin"
}
```

**Response Received:**
```json
{
    "count" : 22,
    "action" : "charcount",
    "type" : "analyzer",
    "ok" : true
}
```

**What I Learned:**
The ‘action’ charcount counts the number of character including spaces.

---

### Attempt 7
**Request Sent:**
```json
{
    "type" : "analyzer",
    "action" : "search",
    "text" : "hello my name is bevin"
}
```

**Response Received:**
```json
{
    "ok" : false,
    "message" : "Field 'find' does not exist in request"
}
```

**What I Learned:**
The ‘action’ search needs field ‘find’ also.

---

### Attempt 8
**Request Sent:**
```json
{
    "type" : "analyzer",
    "action" : "search",
    "text" : "hello my name is bevin",
    "find" : "bev"
}
```

**Response Received:**
```json
{
    "found" : true,
    "find" : "bev",
    "count" : 1,
    "action" : "search",
    "positions" : "[17]",
    "type" : "analyzer",
    "ok" : true
}
```

**What I Learned:**
The 'action' search finds index where string in ‘find’ starts in ‘text’ with starting at 0 index.


---

---

## Part 2: Complete Protocol Specification

Follow the same format as Task 1.1 README protocols.

### [Analyzer]

The analyzer serivice is able to do one of 3 actions for a given 'text' field provided.
 - wordcount: count number of words in 'text'
 - charcount: count number of characters in 'text'
 - search: search 'text' for where string in 'find' starts and provide index and count of how many times

**Request:**
```json
{
    "type" : "analyzer", // type of request
    "action" : "<String>", // action to do -- either "wordcount", "charcount", or "search"
    "text" : "<String>", // text to analyze
    "find" : "<String>" // sub string to search for -- only need if action is "search"
}
```

**General Response:**
```json
{
    "count" : "<int>", // wordcount, charcount, or number of matches for search
    "action" : "<String>", // action done
    "find" : "<String",  // substring searched if action "search"
    "found" : "<bool>", // true if find is in text
    "positions" : "[<int>]", // indexes where found in array 
    "type" : "analyzer", // type of request
    "message" : "<String>", // error message if ok false
    "ok" : "<bool>" // true or false depending on request
}
```

#### [Wordcount]

**Success Resonse for wordcount:**
```json
{
    "count" : "<int>", // number of words in text
    "action" : "wordcount",
    "type" : "analyzer",
    "ok" : "true"
}
```

**Example wordcount Request:**
```json
{
    "type" : "analyzer",
    "action" : "wordcount",
    "text" : "hello my name is bevin"
}
```

**Example wordcount Response:**
```json
{
    "count" : 5,
    "action" : "wordcount",
    "type" : "analyzer",
    "ok" : true
}
```

#### [Charcount]

**Success Resonse for charcount:**
```json
{
    "count" : "<int>", // number of words in text
    "action" : "charcount",
    "type" : "analyzer",
    "ok" : "true"
}
```

**Example charcount Request:**
```json
{
    "type" : "analyzer",
    "action" : "charcount",
    "text" : "hello my name is bevin"
}
```

**Example charcount Response:**
```json
{
    "count" : 22,
    "action" : "charcount",
    "type" : "analyzer",
    "ok" : true
}
```

#### [Search]

**Success Resonse for search:**
```json
{
    "found" : "<bool", // true if found in text
    "find" : "<String>", // substring searched
    "count" : "<int>", // number of matches
    "action" : "search",
    "positions" : "[<int>]", // indexes where found in array 
    "type" : "analyzer",
    "ok" : true
}
```

**Example search Request:**
```json
{
    "type" : "analyzer",
    "action" : "search",
    "text" : "hello my name is bevin",
    "find" : "bev"
}
```

**Example search Response:**
```json
{
    "found" : true,
    "find" : "bev",
    "count" : 1,
    "action" : "search",
    "positions" : "[17]",
    "type" : "analyzer",
    "ok" : true
}
```

#### [Error Cases]

**Missing action field:**
```json
{
    "ok" : false,
    "message" : "Field 'action' does not exist in request. Hint: what action do you want to perform?"
}
```

**Missing text field:**
```json
{
    "ok" : false,
    "message" : "Field 'text' does not exist in request"
}
```

**Invalid action:**
```json
{
    "ok" : false,
    "message" : "Action 'repeat' not supported. Valid actions: wordcount, charcount, search"
}
```

**Missing find field for search:**
```json
{
    "ok" : false,
    "message" : "Field 'find' does not exist in request"
}
```


---

[Document ALL operations you discovered]

The operations I found were wordcount, charcount, and search.

---

## Part 3: Summary

**Total Operations Discovered:** 3
**How I approached discovery:** I used unit tests for discovery. I started with only sending type as analyzer to see what messages were sent from the server. The messaging was clear and pointed out fields that I needed to send with the requests for analyzer. 
**Most challenging part:** The understanding how wordcount and charcount worked was easy. But for search I had to figure out what the positions field returned was. After counting up characters, I found that it was where the sub string started.
