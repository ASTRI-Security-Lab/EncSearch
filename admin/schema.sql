/*
CREATE SEQUENCE kw_id_seq;
CREATE SEQUENCE doc_id_seq;

CREATE TABLE keywords (
	kw_id integer DEFAULT nextval('kw_id_seq'::regclass) NOT NULL,
	name character varying UNIQUE
);

ALTER TABLE ONLY keywords 
	ADD CONSTRAINT keywords_pkey PRIMARY KEY (kw_id);

CREATE TABLE documents (
	doc_id integer DEFAULT nextval('doc_id_seq'::regclass) NOT NULL,
	name character varying UNIQUE
);

ALTER TABLE ONLY documents 
	ADD CONSTRAINT documents_pkey PRIMARY KEY (doc_id);

*/

CREATE TABLE occurs (
	keyword character varying NOT NULL,
	doc_name character varying NOT NULL,
	count integer DEFAULT 1
);

ALTER TABLE ONLY occurs
	ADD CONSTRAINT occurs_pkey PRIMARY KEY (keyword, doc_name);

CREATE INDEX ON occurs (keyword);


CREATE TABLE users (
	username character varying NOT NULL,
	salt character varying NOT NULL
);

ALTER TABLE users
	ADD CONSTRAINT username_pkey PRIMARY KEY (username);
