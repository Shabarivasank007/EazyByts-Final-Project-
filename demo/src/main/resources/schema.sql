-- Drop tables if they exist
DROP TABLE IF EXISTS news;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS news_sources;

-- Create news_sources table
CREATE TABLE news_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    api_url VARCHAR(255),
    rss_url VARCHAR(255),
    description VARCHAR(255),
    logo_url VARCHAR(255),
    country_code VARCHAR(2),
    language_code VARCHAR(5),
    source_type VARCHAR(20) CHECK (source_type IN ('WEB', 'RSS', 'API', 'SOCIAL_MEDIA')),
    api_key VARCHAR(100),
    api_parameters VARCHAR(1000),
    update_frequency_minutes INT DEFAULT 60,
    last_updated TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    priority_level INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_news_sources_name UNIQUE (name),
    CONSTRAINT uk_news_sources_base_url UNIQUE (base_url)
);

-- Create categories table
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    color VARCHAR(7),
    icon_class VARCHAR(50),
    display_order INT DEFAULT 0,
    news_count BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_categories_name UNIQUE (name),
    CONSTRAINT uk_categories_slug UNIQUE (slug)
);

-- Create news table
CREATE TABLE news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    content TEXT,
    image_url VARCHAR(500),
    source_url VARCHAR(500),
    author VARCHAR(100),
    category_id BIGINT NOT NULL,
    source_id BIGINT,
    published_at TIMESTAMP,
    reading_time INT DEFAULT 5,
    view_count BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    is_trending BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_news_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_news_source FOREIGN KEY (source_id) REFERENCES news_sources(id)
);

-- Create indexes
CREATE INDEX idx_news_published_at ON news(published_at);
CREATE INDEX idx_news_category ON news(category_id);
CREATE INDEX idx_news_source ON news(source_id);
CREATE INDEX idx_news_trending ON news(is_trending, view_count);
CREATE INDEX idx_news_slug ON news(slug);
CREATE INDEX idx_categories_slug ON categories(slug);
