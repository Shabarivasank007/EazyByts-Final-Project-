# News Platform - Docker Deployment Guide

## 🚀 Quick Start

### Prerequisites
- Docker installed on your system
- Docker Compose (usually included with Docker Desktop)

### 1. Build and Run with Docker Compose (Recommended)

```bash
# Navigate to project directory
cd demo

# Build and start the application
docker-compose up --build

# Or run in detached mode (background)
docker-compose up --build -d
```

The application will be available at: **http://localhost:8080**

### 2. Build and Run with Docker Only

```bash
# Build the Docker image
docker build -t news-platform .

# Run the container
docker run -p 8080:8080 --name news-platform-app news-platform
```

## 🔧 Configuration

### Environment Variables

You can customize the application by setting environment variables:

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:h2:mem:newsdb
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=

# News API Configuration
NEWS_API_KEY=your_news_api_key_here

# Cache Configuration
SPRING_CACHE_TYPE=caffeine

# Server Configuration
SERVER_PORT=8080
```

### Using MySQL Database

To use MySQL instead of H2, uncomment the MySQL service in `docker-compose.yml` and update the environment variables:

```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/newsplatform
  - SPRING_DATASOURCE_USERNAME=newsuser
  - SPRING_DATASOURCE_PASSWORD=newspassword
  - SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
  - SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.MySQL8Dialect
```

## 🏥 Health Monitoring

### Health Check Endpoint
- **URL:** http://localhost:8080/actuator/health
- **Status:** Returns application health status

### H2 Database Console (Development)
- **URL:** http://localhost:8080/h2-console
- **JDBC URL:** jdbc:h2:mem:newsdb
- **Username:** sa
- **Password:** (leave empty)

## 📱 Application Features

### Available Pages
- **Homepage:** http://localhost:8080/
- **Live News:** http://localhost:8080/news/live
- **Category News:** http://localhost:8080/news/category?category=technology
- **News Detail:** http://localhost:8080/news/{id}
- **Splash Screen:** http://localhost:8080/splash

### API Endpoints
- **GET /api/news/live** - Get live news
- **GET /api/news/category** - Get news by category
- **GET /actuator/health** - Health check

## 🛠️ Development Commands

```bash
# View running containers
docker ps

# View logs
docker-compose logs news-platform

# Stop the application
docker-compose down

# Rebuild and restart
docker-compose up --build

# Remove all containers and volumes
docker-compose down -v
```

## 🔒 Security Notes

- The application runs as a non-root user inside the container
- Health checks are configured for monitoring
- Sensitive data should be passed via environment variables
- For production, consider using Docker secrets for API keys

## 📊 Resource Requirements

- **Memory:** 512MB recommended (256MB minimum)
- **CPU:** 1 core recommended
- **Storage:** ~500MB for image + application data

## 🚀 Production Deployment

For production deployment:

1. Set appropriate environment variables
2. Use external database (MySQL/PostgreSQL)
3. Configure proper logging
4. Set up reverse proxy (Nginx)
5. Use Docker secrets for sensitive data
6. Configure monitoring and alerting

## 🐛 Troubleshooting

### Common Issues

1. **Port already in use:**
   ```bash
   # Change port in docker-compose.yml
   ports:
     - "8081:8080"  # Use port 8081 instead
   ```

2. **Build failures:**
   ```bash
   # Clean build
   docker-compose down
   docker system prune -f
   docker-compose up --build
   ```

3. **Memory issues:**
   ```bash
   # Increase memory limit
   environment:
     - JAVA_OPTS=-Xmx1g -Xms512m
   ```

## 📞 Support

If you encounter any issues:
1. Check the application logs: `docker-compose logs`
2. Verify all environment variables are set correctly
3. Ensure Docker has sufficient resources allocated
4. Check the health endpoint: http://localhost:8080/actuator/health
