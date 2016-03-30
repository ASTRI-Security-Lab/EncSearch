# Search Over Encrypted Data Project Requirements #

*Approved:* Roman Plášil, 29th March 2016

The following components shall be implemented for this project:

* JVM based PC client for encryption and search
* JVM based server providing search REST API
* mobile iOS client for encrypted search

## JVM based PC client for encryption and search ##

This component should have the following functionality:

* Let the user add documents of TXT, DOC, DOCX or PDF format
* Each document should be encrypted using user's password
* Before encryption, individual words should be extracted from the document
* The words should be transformed using the user's password using a one-way function
* The transformed words, together with identification of the source document,
  should be uploaded to the server.

* Later, the user can post queries to the server
* The query is a list of words, each of them transformed using the same one-way
  function and the user's password
* The server should return a list of document ids that contain all of the
  queried words based on the index it's storing
* The client application shall decrypt the documents found and identified by
  the server so that the user can work with them.

## JVM based server for providing search REST API ##

This component stores a list of transformed words and document ids in a SQL database.
Each row `(w, d)` in the database means that word `w` occurs in document `d`.
The server also keeps a database of users where each row `(u, s)` corresponds
to one user with username `u` and PBKDF salt `s`.

### The server API ###

Byte arrays are base64 encoded. Requests and responses are JSON.

#### PUT `/index` ####

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

#### PUT `/search` ####

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


#### GET `/user/{username}` ####

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


