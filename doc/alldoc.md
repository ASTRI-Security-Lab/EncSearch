# Search Over Encrypted Data Project Design #

The following components were implemented and tested for this project:

* JVM based PC client for encryption and search
* JVM based server providing search REST API
* mobile iOS client for encrypted search



## JVM based PC client for encryption and search ##

The purpose of this application is letting users import their document
files for encryption and indexing. After that, the encrypted files can be
uploaded to any untrusted cloud provider and the index will be sent to
the search server.

The same application can also be used to perform search for documents
based on keywords on the search server and to decrypt documents that match.

Currently supported file formats are TXT, DOC, DOCX, PDF. We use external
libraries for reading them so the level of compatibility depends on them.

### Usage ###

The application's functions are invoked using a command line interface.
Without any parameters, it'll only display basic usage help:

```> EncSearch
init | add | q
```

The first step is to use the `init` option to create a configuration file
for the current user. This will create a configuration file `config.json`
in the app data folder `~/.org.astri.snds.encsearch`. The file contains 
default options for all values and should be edited if change to search
server URL, username or document folders is required.


In the next step, user can add documents for indexing and encryption. Upon request,
the application will automatically index and encrypt documents in the "Docs Path"
location (this path can be changed in the config file). Encryption requires
the user to enter a password. Of course this password should be secure enough
and should not be forgotten.

The command `EncSearch add` will ask for user's password and then it will process
new documents in the "Docs Path", all new files will be indexed, encrypted and
the resulting encrypted file saved in the "Encrypted Path" which by default is 
`~/.org.astri.snds.encsearch/encrypted` but can be changed in the config file.

Running the app looks like this:

```
Please type your password
....
Processing file Documents/cats.pdf
```

The last option is `q` which means simply querying for encrypted documents matching 
a keyword. The keywords to search for should be given as command line arguments such
as in the following:

```
EncSearch q cats dogs
```

which will search for documents containing both the keyword "cat" and "dog". The app
will prompt the user to enter his password again. Without a correct password, no
document will match.



## JVM based server providing search REST API ##

This part of the project is implemented in Scala and is designed to run in Java Servlet
containers such as Tomcat. We've used Tomcat 7 running on Java version 8. For data
storage, the application uses a PostgreSQL database.

The database simply stores information about which document contains what keywords.
Document names are encrypted and keywords are transformed using a keyed pseudorandom
function (see section Security design below).

### Database setup ###

The application requires a database with the name `encsearch` to be available in
the PostgreSQL server running on the same machine. The database needs to be accessible
to the user under which the server is running. The file `admin/schema.sql` contains
code to create tables and indices as needed.

### API description ###

Description of the REST-like APIs provided by the Search Server is available
in the source code folder `doc/apis.md` in Markdown format.


# Security design #

## Threat model ##

The application is designed to protect documents stored with an untrusted cloud
storage provider against information theft and tampering. The search
functionality is designed to protect against leaking information about:

* the contents of the encrypted documents
* the names of encrypted documents
* the searched keywords themselves

We assume the computer where the client application is run is safe and data sent out
does not leak any of the information.

## File encryption ##

The encryption keys are derived from the password given by the user. The
derivation uses PBKDF2 with SHA-256 with 128 000 rounds and a randomly
generated salt for each user.  This is to prevent dictionary and limit brute
force attacks. The output of PBKDF2 are 4 keys of 16 bytes each:

* file contents key
* file contents HMAC key
* file name key
* keyword key

Each file is encrypted using AES in CBC mode with a random IV using the "file
contents key".  After encryption, the data goes to HMAC-SHA256 (which uses the
"file contents HMAC key") in order to generate an authentication tag to prevent
tampering and possible chose ciphertext attacks.

File name is encrypted using AES in CBC mode using the same IV, the "file name
key" and also HMAC'ed using "file contents HMAC key".

The extra metadata generated during encryption is saved in a JSON file on disk
next to encrypted file as well to the Search Server. The items are as follows
(they are all public):

* IV
* PBKDF2 iteration count
* crypto version
* salt
* hmac of file contents
* hmac of file name


## Encrypted index ##

The keywords extracted from input documents are all transformed using a keyed
pseudorandom function into byte strings to be stored in the search index. The
key is obtained as in previous section using PBKDF2 from user's password and
salt. The keyed pseudorandom function used is HMAC SHA256. We make use of its
properties as follows:

* deterministic (always the same output for same inputs)
* non forgeable (impossible to find the output without knowing the key)
* very unlikely collisions (two keywords unlikely to produce the same output)
* preimage resistance (cannot find the original keyword or key from the output)


# Testing #

The internal implementation is covered by unit tests distributed with the project.
Running them is as simple as executing the Gradle build tool:

```
> gradle test

... some output ...
ExtractSpec:
- should be normalized lowercase singular (1 second, 27 milliseconds)
Document extractor
- should extract TXT correctly (44 milliseconds)
Document extractor
- should extract PDF correctly (1 second, 419 milliseconds)
Document extractor
- should extract DOC correctly (391 milliseconds)
Document extractor
- should extract DOCX correctly (635 milliseconds)
crypto and encoding
- should work correctly (13 milliseconds)


FullSpec:
Whole system
- should just work (2 seconds, 316 milliseconds)
File encryption
- should just work (2 seconds, 470 milliseconds)

```

## Unit tests for client application ##

* _ExtractSpec_: tests extracting keywords from documents of all supported types (DOC, TXT, DOCX, PDF).
* _ExtractSpec_:_crypto_: tests that password based key derivation produces the same key as a different library
* _FullSpec_:_Whole system_: tests that documents are found, keywords extracted and uploaded and can be queried correctly
* _FullSpec_:_File encryption_: tests that the app will find & encrypt files in the source folder
	

## Unit tests for search server ##

* _IndexSpec_:_insertion_ tests inserting keyword occurrences into the index using the public API
* _IndexSpec_:_search correctly_ tests that search using public API returns results as expected
* _IndexSpec_:_search multiple keywords_ tests that search returns only documents that contain all keywords that
  were searched for
* _IndexSpec_:_not find not existing keywords_ tests that the API returns 0 results for keywords that do not occur
  in the database
