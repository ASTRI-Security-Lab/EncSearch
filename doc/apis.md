POST `/index/add`
--------------------
```
{
	doc_ids: {
		<document name>: int	 // each document name gets a numeric ID assigned
	}
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

Database schema:
-------------------
	TABLE keywords
		id, name
	TABLE documents
		id, name
	TABLE occurs
		kw_id, doc_id, count
