CREATE TABLE mykeyspace.resource_location(
  resource_location_id UUID,
  resource_uri VARCHAR,
  resource_display_location VARCHAR,
  resource_name VARCHAR,
  resource_walker_name VARCHAR,
  resource_indexer_name VARCHAR,
  resource_last_modified timestamp,
  resource_size DOUBLE,
  index_generated timestamp,
  PRIMARY KEY (resource_location_id));

CREATE TABLE mykeyspace.word_index(
  resource_location_id UUID,
  word VARCHAR,
  indices Map<INT, INT>,
  content VARCHAR,
  prev_content VARCHAR,
  next_content VARCHAR,
  content_key1 VARCHAR,
  content_key2 VARCHAR,
  content_key3 VARCHAR,
  resource_uri VARCHAR,
  resource_display_location VARCHAR,
  resource_name VARCHAR,
  resource_walker_name VARCHAR,
  resource_indexer_name VARCHAR,
  resource_last_modified timestamp,
  resource_size DOUBLE,
  PRIMARY KEY(resource_location_id, word)
);

CREATE TYPE mykeyspace.word_index(
  resource_location_id UUID,
  word VARCHAR,
  indices Map<INT, INT>,
  content VARCHAR,
  prev_content VARCHAR,
  next_content VARCHAR,
  content_key1 VARCHAR,
  content_key2 VARCHAR,
  content_key3 VARCHAR,
  resource_display_location VARCHAR,
  resource_uri VARCHAR,
  resource_name VARCHAR,
  resource_walker_name VARCHAR,
  resource_indexer_name VARCHAR,
  resource_last_modified timestamp,
  resource_size DOUBLE
);

CREATE TABLE mykeyspace.word_index_by_key(
  key_id VARCHAR,
  search_key VARCHAR,
  result List<word_index>
  PRIMARY KEY(word, index_type, search_key)
);
