# NakanoStay

> **Sistema de gestión de habitaciones de hotel con API REST**

NakanoStay es una API REST completa para la gestión de hoteles, habitaciones y reservas. El sistema incluye funcionalidades de CRUD, gestión de estados de reservas, notificaciones por email y autenticación robusta.

## 🏗️ Arquitectura

El proyecto implementa una **arquitectura en capas** que garantiza la separación de responsabilidades y facilita el mantenimiento:

- **Controladores** (`controllers/`): Manejan las peticiones HTTP y respuestas
- **Servicios** (`services/`): Contienen la lógica de negocio
- **Repositorios** (`repositories/`): Acceso a datos con Spring Data JPA
- **Entidades** (`models/entities/`): Modelos de dominio con JPA
- **DTOs** (`models/requests/responses/`): Objetos de transferencia de datos
- **Mappers** (`mappers/`): Conversión entre entidades y DTOs
- **Configuración** (`config/`): Configuración de seguridad y otros aspectos

## 🛠️ Tecnologías

### Backend
- **Kotlin** + **Spring Boot**
- **Spring Data JPA** para persistencia
- **Spring Security** para autenticación/autorización
- **Spring Mail** para envío de emails
- **Thymeleaf** para templates de email
- **Resilience4j** para rate limiting

### Base de Datos
- **PostgreSQL**
- **JPA/Hibernate** como ORM

### Autenticación
- **Supabase** como proveedor de autenticación externa
- Configuración para autenticación **JWT**

### Despliegue
- **Docker** para inicializar la aplicación y la base de datos en contenedores
- **AWS EC2** para hostear la aplicación en la nube

## 📚 Documentación de la API

### 🏨 Hoteles

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| `GET` | `/api/hotels` | Obtener todos los hoteles | Pública |
| `GET` | `/api/hotels/{id}` | Obtener hotel por ID | Pública |
| `POST` | `/api/hotels` | Crear nuevo hotel | Admin |
| `PUT` | `/api/hotels/{id}` | Actualizar hotel | Admin |
| `DELETE` | `/api/hotels/delete/{id}` | Eliminar hotel | Admin |

#### Crear Hotel - `POST /api/hotels`

**Request Body:**
```json
{
  "name": "string (requerido)",
  "address": "string (requerido)",
  "city": "string (opcional)",
  "stars": "int (opcional)",
  "email": "string (requerido)"
}
```

**Response:**
```json
{
  "id": "long",
  "name": "string",
  "address": "string",
  "city": "string (opcional)",
  "stars": "int (opcional)",
  "email": "string"
}
```

### 🛏️ Habitaciones

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| `GET` | `/api/rooms` | Obtener todas las habitaciones | Pública |
| `GET` | `/api/rooms/hotel/{hotelId}` | Obtener habitaciones por hotel | Pública |
| `GET` | `/api/rooms/{id}/availability?startDate=&endDate=` | Revisar disponibilidad | Pública |
| `POST` | `/api/rooms` | Crear nueva habitación | Admin |
| `PUT` | `/api/rooms/{id}` | Actualizar habitación | Admin |
| `PUT` | `/api/rooms/{id}/available` | Marcar como disponible | Admin |
| `PUT` | `/api/rooms/{id}/unavailable` | Marcar como no disponible | Admin |
| `DELETE` | `/api/rooms/delete/{id}` | Eliminar habitación | Admin |

#### Crear Habitación - `POST /api/rooms`

**Request Body:**
```json
{
  "hotel_id": "long (requerido)",
  "room_number": "string (requerido)",
  "room_type": "string (opcional)",
  "price_per_night": "decimal (requerido)",
  "is_available": "boolean (requerido)"
}
```

#### Consultar Disponibilidad - `GET /api/rooms/{id}/availability`

**Response:**
```json
{
  "room_id": "long",
  "available_dates": ["date"],
  "occupied_ranges": [
    {
      "start": "date",
      "end": "date"
    }
  ]
}
```

### 📅 Reservas

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| `GET` | `/api/bookings` | Obtener todas las reservas | Admin |
| `GET` | `/api/bookings/{id}` | Obtener reserva por ID | Admin |
| `GET` | `/api/bookings/code/{code}?dni={dni}` | Buscar reserva por código y DNI | Pública |
| `POST` | `/api/bookings` | Crear nueva reserva | Pública |
| `PUT` | `/api/bookings/code/{code}/cancel?dni={dni}` | Cancelar reserva | Pública |
| `PUT` | `/api/bookings/code/{code}/confirm?dni={dni}` | Confirmar reserva | Admin |
| `PUT` | `/api/bookings/code/{code}/complete?dni={dni}` | Completar reserva | Admin |
| `DELETE` | `/api/bookings/delete/{id}` | Eliminar reserva | Admin |

#### Crear Reserva - `POST /api/bookings`

**Request Body:**
```json
{
  "guest_name": "string (requerido)",
  "guest_dni": "string (requerido)",
  "guest_email": "string (requerido)",
  "guest_phone": "string (opcional)",
  "check_in": "date (YYYY-MM-DD, requerido)",
  "check_out": "date (YYYY-MM-DD, requerido)",
  "status": "string (default=PENDING)",
  "details": [
    {
      "room_id": "long (requerido)",
      "guests": "int (requerido)"
    }
  ]
}
```

**Response:**
```json
{
  "id": "long",
  "booking_code": "string",
  "guest_name": "string",
  "guest_dni": "string",
  "guest_email": "string",
  "guest_phone": "string (opcional)",
  "booking_date": "datetime (ISO-8601)",
  "check_in": "date",
  "check_out": "date",
  "status": "enum BookingStatus",
  "total": "decimal",
  "details": [
    {
      "room_id": "long",
      "guests": "int",
      "price_at_booking": "decimal"
    }
  ]
}
```

## 🔄 Estados de Reserva

- **PENDING**: Reserva creada, pendiente de confirmación
- **CONFIRMED**: Reserva confirmada por el hotel
- **CANCELLED**: Reserva cancelada
- **COMPLETED**: Reserva completada (check-out realizado)

4. La API está disponible en `http://34.207.200.47:8080/`

## 🔧 Configuración

### Variables de Entorno

Asegúrate de configurar las siguientes variables:

- `DATABASE_URL`: URL de conexión a PostgreSQL
- `SUPABASE_URL`: URL de tu proyecto Supabase
- `SUPABASE_KEY`: Clave de API de Supabase
- `MAIL_HOST`: Servidor SMTP para envío de emails
- `MAIL_PORT`: Puerto del servidor SMTP
- `MAIL_USERNAME`: Usuario del email
- `MAIL_PASSWORD`: Contraseña del email

## 📈 Códigos de Respuesta HTTP

| Código | Descripción |
|--------|-------------|
| `200` | Operación exitosa |
| `400` | Datos inválidos |
| `404` | Recurso no encontrado |
| `409` | Conflicto (datos duplicados) |

## 🤝 Contribuciones

Este proyecto fue desarrollado como parte de un aprendizaje en arquitectura empresarial usando Spring Boot con Kotlin.


## 📧 Autor

**Jeremy Marin** - Desarrollo de Backend con Spring Boot y Kotlin

---

*Este proyecto fue desarrollado como parte del aprendizaje en arquitectura empresarial, enfocándose en las mejores prácticas de desarrollo backend con Spring Boot y Kotlin.*
