PUT `/index`
--------------------
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
