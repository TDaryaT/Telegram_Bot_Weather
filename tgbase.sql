DROP TABLE IF EXISTS users, weather;

CREATE TABLE IF NOT EXISTS users (
	user_id		int primary key,
	city 			varchar(30) 
);

CREATE TABLE IF NOT EXISTS weather (
	city 			varchar(30) primary key,
	condition   	varchar(20),
	temp 			real,
	temp_like  	real,
	wind_speed     real,
	date 		        date 
);