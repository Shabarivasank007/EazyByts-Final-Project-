-- Insert default categories
INSERT INTO categories (name, slug, description, color, icon_class, display_order, is_active, is_featured) VALUES
('World News', 'world-news', 'Latest news from around the world', '#1a73e8', 'fas fa-globe', 1, true, true),
('Technology', 'technology', 'Latest technology news and updates', '#34a853', 'fas fa-microchip', 2, true, true),
('Business', 'business', 'Business and financial news', '#fbbc04', 'fas fa-chart-line', 3, true, true),
('Sports', 'sports', 'Sports news and updates', '#ea4335', 'fas fa-football-ball', 4, true, true),
('Entertainment', 'entertainment', 'Entertainment and celebrity news', '#9334e8', 'fas fa-film', 5, true, true),
('Science', 'science', 'Science and research news', '#16a085', 'fas fa-flask', 6, true, false),
('Health', 'health', 'Health and medical news', '#2ecc71', 'fas fa-heartbeat', 7, true, false),
('Politics', 'politics', 'Political news and updates', '#e74c3c', 'fas fa-landmark', 8, true, false);

-- Insert default news sources
INSERT INTO news_sources (name, base_url, description, logo_url, source_type, priority_level, is_active) VALUES
('Reuters', 'https://www.reuters.com', 'Leading international news agency', '/images/sources/reuters.png', 'RSS', 1, true),
('Associated Press', 'https://www.ap.org', 'Nonprofit news cooperative', '/images/sources/ap.png', 'RSS', 2, true),
('Tech Crunch', 'https://techcrunch.com', 'Technology news and analysis', '/images/sources/techcrunch.png', 'RSS', 3, true),
('BBC News', 'https://www.bbc.com/news', 'British Broadcasting Corporation', '/images/sources/bbc.png', 'RSS', 4, true);

-- Insert sample news articles
INSERT INTO news (title, slug, description, content, image_url, category_id, source_id, published_at, is_active, is_featured, reading_time)
SELECT
    'Welcome to Our News Platform',
    'welcome-to-our-news-platform',
    'Discover the latest news from around the world with our comprehensive news platform.',
    'Welcome to our news platform! We are excited to bring you the latest news from around the world. Our platform features comprehensive coverage of world events, technology updates, business news, and much more.',
    '/images/placeholder/news-default.jpg',
    c.id,
    s.id,
    CURRENT_TIMESTAMP,
    true,
    true,
    5
FROM categories c, news_sources s
WHERE c.slug = 'world-news' AND s.name = 'Reuters'
LIMIT 1;

-- Insert 15 sample news articles
INSERT INTO news (title, slug, description, content, image_url, category_id, source_id, published_at, is_active, is_featured, reading_time) VALUES
('Global Markets Rally', 'global-markets-rally', 'Markets around the world see a major rally.', 'Full content for Global Markets Rally.', 'https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=1200&h=600&fit=crop', 1, 1, CURRENT_TIMESTAMP - INTERVAL '1' DAY, true, false, 3),
('Tech Giants Announce New Devices', 'tech-giants-announce-new-devices', 'Major tech companies unveil new products.', 'Full content for Tech Giants.', 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=1200&h=600&fit=crop', 2, 3, CURRENT_TIMESTAMP - INTERVAL '2' DAY, true, false, 4),
('Business Leaders Meet in Davos', 'business-leaders-meet-davos', 'Top business leaders gather for annual summit.', 'Full content for Business Leaders.', 'https://images.unsplash.com/photo-1444653614773-995cb1ef9efa?w=1200&h=600&fit=crop', 3, 1, CURRENT_TIMESTAMP - INTERVAL '3' DAY, true, false, 2),
('Championship Finals Results', 'championship-finals-results', 'Exciting results from the championship finals.', 'Full content for Championship.', 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=1200&h=600&fit=crop', 4, 2, CURRENT_TIMESTAMP - INTERVAL '4' DAY, true, false, 3),
('Entertainment Awards Highlights', 'entertainment-awards-highlights', 'Major moments from the entertainment awards.', 'Full content for Entertainment.', 'https://images.unsplash.com/photo-1586899028174-e7098604235b?w=1200&h=600&fit=crop', 5, 4, CURRENT_TIMESTAMP - INTERVAL '5' DAY, true, false, 2),
('Science Breakthrough Announced', 'science-breakthrough-announced', 'Scientists announce major research breakthrough.', 'Full content for Science News.', 'https://images.unsplash.com/photo-1507413245164-6160d8298b31?w=1200&h=600&fit=crop', 6, 1, CURRENT_TIMESTAMP - INTERVAL '6' DAY, true, false, 5),
('Health Study Reveals New Findings', 'health-study-reveals-findings', 'New health study changes medical understanding.', 'Full content for Health Study.', 'https://images.unsplash.com/photo-1532938911079-1b06ac7ceec7?w=1200&h=600&fit=crop', 7, 2, CURRENT_TIMESTAMP - INTERVAL '7' DAY, true, false, 2),
('Political Summit Outcomes', 'political-summit-outcomes', 'Key decisions from the political summit.', 'Full content for Political Summit.', 'https://images.unsplash.com/photo-1529107386315-e1a2ed48a620?w=1200&h=600&fit=crop', 8, 3, CURRENT_TIMESTAMP - INTERVAL '8' DAY, true, false, 3),
('Space Mission Success', 'space-mission-success', 'Historic success in latest space mission.', 'Full content for Space Mission.', 'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1200&h=600&fit=crop', 6, 4, CURRENT_TIMESTAMP - INTERVAL '9' DAY, true, false, 4),
('Movie Industry Records', 'movie-industry-records', 'Box office records shattered this weekend.', 'Full content for Movie Industry.', 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200&h=600&fit=crop', 5, 1, CURRENT_TIMESTAMP - INTERVAL '10' DAY, true, false, 2),
('Technology Innovation Award', 'technology-innovation-award', 'Breakthrough technology wins global award.', 'Full content for Technology Innovation.', 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200&h=600&fit=crop', 2, 2, CURRENT_TIMESTAMP - INTERVAL '11' DAY, true, false, 3),
('Healthcare Conference', 'healthcare-conference', 'Global healthcare leaders meet.', 'Full content for Healthcare Conference.', 'https://images.unsplash.com/photo-1631815589068-dc3f1629fb10?w=1200&h=600&fit=crop', 7, 3, CURRENT_TIMESTAMP - INTERVAL '12' DAY, true, false, 4),
('International Politics Update', 'international-politics-update', 'Latest developments in international relations.', 'Full content for Politics Update.', 'https://images.unsplash.com/photo-1541872703-74c5e44368f9?w=1200&h=600&fit=crop', 8, 4, CURRENT_TIMESTAMP - INTERVAL '13' DAY, true, false, 2),
('Scientific Discovery', 'scientific-discovery', 'Groundbreaking scientific discovery announced.', 'Full content for Scientific Discovery.', 'https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=1200&h=600&fit=crop', 6, 1, CURRENT_TIMESTAMP - INTERVAL '14' DAY, true, false, 3),
('Global News Update', 'global-news-update', 'Latest updates from around the world.', 'Full content for Global News.', 'https://images.unsplash.com/photo-1521295121783-8a321d551ad2?w=1200&h=600&fit=crop', 1, 2, CURRENT_TIMESTAMP - INTERVAL '15' DAY, true, false, 2);

-- Only 3 featured stories with working online images
DELETE FROM news WHERE is_featured = true;
INSERT INTO news (title, slug, description, content, image_url, category_id, source_id, published_at, is_active, is_featured, reading_time) VALUES
('AI Revolution in Healthcare', 'ai-revolution-healthcare', 'Artificial Intelligence is transforming the healthcare industry.', 'Full content about AI in healthcare.', 'https://images.unsplash.com/photo-1581093588401-7b8c5e1c1a1a?w=1200&h=600&fit=crop', 7, 3, CURRENT_TIMESTAMP - INTERVAL '1' DAY, true, true, 5),
('Mars Mission: A Giant Leap', 'mars-mission-giant-leap', 'The latest updates on the Mars exploration mission.', 'Full content about Mars mission.', 'https://images.unsplash.com/photo-1581092339787-2d5a5b1c1a1a?w=1200&h=600&fit=crop', 6, 4, CURRENT_TIMESTAMP - INTERVAL '2' DAY, true, true, 6),
('Global Climate Summit 2025', 'global-climate-summit-2025', 'World leaders discuss climate change solutions.', 'Full content about the climate summit.', 'https://images.unsplash.com/photo-1573497491208-6b1acb260507?w=1200&h=600&fit=crop', 1, 2, CURRENT_TIMESTAMP - INTERVAL '3' DAY, true, true, 4);
