DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
	user_id		    int primary key,
	lat             real,
	lon             real
);