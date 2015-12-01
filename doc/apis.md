Byte arrays are base64 encoded. Requests and responses are JSON.


PUT `/index`
--------------------
Used to upload a document (encrypted) index
request:
```
{
	username: string  // user identifier TODO: add auth to prevent random people from adding to index
	salt: string      // the salt this user is using for his password. The server stores it.
	doc_ids: [String] // each document name gets a numeric ID assigned
	keywords: {
		<keyword>: {
			<document id>: int   // number of occurences in given document
		}
	}
}
```

POST `/search`
--------------------
request:
```
{
	username: string	// to search only within documents belonging to me
	keywords: [string]  // to search documents which contain all of these keywords
}
```

response:
```
{
	result: true
	documents: [String]   // list of document names that matched
}
```


GET `/user/{username}`
----------------------
Used to get information about a user
request: empty, just GET

response:
```
{
	result: true,
	user: {
		username: string
		salt: string
	}
}
```


