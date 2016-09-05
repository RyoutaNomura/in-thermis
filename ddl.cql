CREATE TABLE mykeyspace.resource_location(
  resource_location_id UUID,
  resource_uri VARCHAR,
  resource_display_location VARCHAR,
  resource_name VARCHAR,
  resource_size BIGINT,
  resource_walker_name VARCHAR,
  resource_indexer_name VARCHAR,
  resource_last_modified timestamp,
  index_generated timestamp,
  PRIMARY KEY (resource_location_id));

CREATE TABLE mykeyspace.resource_content(
  resource_content_id UUID, 
  key1 VARCHAR,
  key2 VARCHAR,
  key3 VARCHAR,
  content VARCHAR,
  prev_content VARCHAR,
  next_content VARCHAR,
  resource_location_id UUID, 
  PRIMARY KEY (resource_content_id));

CREATE TABLE mykeyspace.word_index(
  word VARCHAR,
  resource_content_id UUID,
  indices Map<INT, INT>,
  indices_count INT,
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
  resource_size BIGINT,
  resource_location_id UUID,
  PRIMARY KEY(word, resource_content_id)
);

create INDEX resource_content_by_location_id on mykeyspace.resource_content(resource_location_id);
create INDEX resource_location_by_walker on mykeyspace.resource_location(resource_walker_name);
create INDEX word_index_by_location_id on mykeyspace.word_index(resource_location_id);
CREATE INDEX resource_location_by_uri ON mykeyspace.resource_location(resource_uri);
CREATE INDEX word_index_by_walker ON mykeyspace.word_index(resource_walker_name);
CREATE INDEX word_index_by_indexer ON mykeyspace.word_index(resource_indexer_name);
CREATE INDEX word_index_by_modified_date ON mykeyspace.word_index(resource_last_modified);


