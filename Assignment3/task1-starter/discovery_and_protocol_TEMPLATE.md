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
    "ok" : false,\
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
    "positions" : [17],
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

### [Service Name]

[Brief description of what the service does]

#### [Operation Name]

**Request:**
```json
{

}
```

**Success Response:**
```json
{

}
```

**Error Responses:**

```json
{

}
```

---

[Document ALL operations you discovered]

---

## Part 3: Summary

**Total Operations Discovered:**
**How I approached discovery:**
**Most challenging part:**
