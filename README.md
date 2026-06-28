# Backend Taller Automotriz — TheBoys

para documentacion api y entregables ver en :
https://1drv.ms/f/c/4767A769CDC6DA11/IgAjEBWcpV2FSpNwY7RbVZMNAT-TWC6rTUNJVOhzyHIRD18?e=mLneHD

Sistema de gestión de un taller automotriz desarrollado con **Spring Boot 4.0.6** y **PostgreSQL** (Supabase). Incluye autenticación JWT, RBAC con 3 roles, integración con Stripe, generación automática de facturas con IVA, notificaciones y reportes.

---
## Tabla de Contenidos

- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Variables de entorno y configuración](#variables-de-entorno-y-configuración)
- [Cómo correr el proyecto](#cómo-correr-el-proyecto)
- [Modelo de datos](#modelo-de-datos)
- [Seguridad y autenticación](#seguridad-y-autenticación)
- [Roles y permisos](#roles-y-permisos)
- [Enumeraciones](#enumeraciones)
- [Manejo de errores](#manejo-de-errores)
- [Resumen de endpoints](#resumen-de-endpoints)
- [Flujos de negocio importantes](#flujos-de-negocio-importantes)
- [CORS](#cors)

---

## Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje |
| Spring Boot | 4.0.6 | Framework principal |
| Spring Security | (incluido) | Auth + RBAC |
| Spring Data JPA | (incluido) | ORM / acceso a datos |
| PostgreSQL | (Supabase) | Base de datos |
| JJWT | 0.12.6 | JWT |
| Stripe Java SDK | 32.1.0 | Pagos con tarjeta |
| Lombok | (incluido) | Reducción de boilerplate |
| Jackson | 2.15.2 | Serialización JSON |
| Bean Validation | (incluido) | Validación de DTOs |

---

## Arquitectura

El proyecto sigue **arquitectura N capas**:

```
Controller → Service → Repository → Entity (JPA)
```

Cada capa se comunica con la siguiente únicamente a través de DTOs o entidades JPA. Los controladores nunca exponen entidades directamente.

```
src/main/java/com/example/backend_tallerautomotriz/
├── config/          # SecurityConfig, CorsConfig
├── controller/      # REST controllers
├── dto/
│   ├── request/     # DTOs de entrada (validados con @Valid)
│   └── response/    # DTOs de salida
├── entity/          # Entidades JPA
├── enums/           # Enums del dominio
├── exception/       # Excepciones custom + GlobalExceptionHandler
├── repository/      # Interfaces JPA Repository
├── security/        # JwtTokenProvider, JwtAuthenticationFilter
└── service/         # Lógica de negocio
```

---

## Estructura del proyecto

```
Backend_TallerAutomotriz/
├── src/
│   ├── main/
│   │   ├── java/com/example/backend_tallerautomotriz/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```

---

## Variables de entorno y configuración

El archivo `application.properties` usa placeholders para secretos sensibles. Las siguientes variables deben configurarse en el entorno (o en el archivo directamente para desarrollo local):

```properties
# Base de datos (Supabase session pooler — puerto 5432)
spring.datasource.url=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?preparedStatementCacheQueries=0&preparedStatementCacheSqlLimit=0
spring.datasource.username=postgres.<proyecto>
spring.datasource.password=<tu_password>

# JWT
app.jwt.secret=<string_largo_aleatorio>
app.jwt.expiration-ms=86400000   # 24 horas

# Stripe
stripe.api-key=${STRIPE_SECRET_KEY:}
stripe.publishable-key=${STRIPE_PUBLISHABLE_KEY:}

# Servidor
server.port=4000
```

> ⚠️ **Nota sobre Supabase:** usar siempre el **session pooler** en puerto **5432**, no el transaction pooler (6543), para evitar problemas de compatibilidad con Hibernate prepared statements.

### Configuración de HikariCP

El pool está ajustado para conexiones a Supabase con límites bajos para no agotar las conexiones disponibles:

```properties
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=60000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000
```

### DDL

```properties
spring.jpa.hibernate.ddl-auto=update
```

Hibernate crea/actualiza las tablas automáticamente al arrancar. Para producción se recomienda cambiar a `validate` y gestionar migraciones con Flyway/Liquibase.

---

## Cómo correr el proyecto

### Requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL o acceso a un proyecto Supabase

### Pasos

```bash
# 1. Clonar el repositorio
git clone <repo-url>
cd Backend_TallerAutomotriz

# 2. Configurar variables sensibles en application.properties
#    o exportar como variables de entorno:
export STRIPE_SECRET_KEY=sk_test_...
export STRIPE_PUBLISHABLE_KEY=pk_test_...

# 3. Compilar y ejecutar
./mvnw spring-boot:run

# El servidor arranca en http://localhost:4000 en produccion en https://frontend-pnc-proyecto-the-boys.vercel.app/

```

### Base URL

```
http://localhost:4000/api/v1 
deployado :https://backend-pnc-proyecto-theboys.onrender.com/api/v1
```

---

## Modelo de datos

### Entidades principales

| Entidad | Tabla | Descripción |
|---|---|---|
| `Usuario` | `usuario` | Cuenta de acceso. Campos: email, password (BCrypt), nombre, apellido, rol, intentosFallidos, bloqueado |
| `Rol` | `rol` | Catálogo de roles (ADMIN, MECANICO, CLIENTE) |
| `Sucursal` | `sucursal` | Sede física del taller. Campos: nombre, dirección, departamento |
| `Cliente` | `cliente` | Perfil cliente ligado a un Usuario. Campos: usuario, teléfono, dirección |
| `Mecanico` | `mecanico` | Perfil mecánico ligado a un Usuario y a una Sucursal |
| `Vehiculo` | `vehiculo` | PK: patente (String). Campos: marca, modelo, cliente |
| `Servicio` | `servicio` | Catálogo de servicios. Campos: nombre, descripción, tiempoEstimadoMinutos, precioBase, estado |
| `Proveedor` | `proveedor` | Campos: nombre, marca, contacto |
| `Repuesto` | `repuesto` | Campos: nombre, precioUnitario, categoría (enum), descripción, proveedor |
| `Inventario` | `inventario` | Stock por sucursal. Campos: sucursal, repuesto, stockTotal, fechaActualización |
| `OrdenTrabajo` | `orden_trabajo` | Orden de reparación. Campos: vehículo, cliente, mecánico, sucursal, tipoOrden, estado, fechaCreación, fechaFinalizaciónEstimada, comentarios, presupuestoTotal |
| `OrdenServicio` | `orden_servicio` | Tabla pivote orden ↔ servicio. PK compuesta: (ordenId, servicioId). Campo: precioAplicado |
| `OrdenRepuesto` | `orden_repuesto` | Tabla pivote orden ↔ repuesto. PK compuesta: (ordenId, repuestoId). Campos: cantidad, precioAplicado |
| `RegistroHoras` | `registro_horas` | Horas trabajadas por mecánico en una orden |
| `Factura` | `factura` | Generada automáticamente al completar una orden. Campos: subtotal, impuestos (IVA 13%), total, estadoPago, metodoPago |
| `Cita` | `cita` | Agenda de atención. Campos: cliente, sucursal, mecánico (opcional), fecha, hora, estado, tipoOrden, nuevaFechaPropuesta, nuevaHoraPropuesta |
| `Notificacion` | `notificacion` | Mensajes internos al usuario. Campos: mensaje, leida, fechaCreación, tipo, referenciaId |

### Relaciones clave

```
Usuario 1──1 Cliente
Usuario 1──1 Mecanico
Mecanico N──1 Sucursal
Vehiculo N──1 Cliente
OrdenTrabajo N──1 Vehiculo
OrdenTrabajo N──1 Cliente
OrdenTrabajo N──1 Mecanico
OrdenTrabajo N──1 Sucursal
OrdenTrabajo N──M Servicio  (via OrdenServicio)
OrdenTrabajo N──M Repuesto  (via OrdenRepuesto)
OrdenTrabajo 1──1 Factura
Repuesto N──1 Proveedor
Inventario N──1 Sucursal
Inventario N──1 Repuesto
Cita N──1 Cliente
Cita N──1 Sucursal
Cita N──1 Mecanico (nullable)
Notificacion N──1 Usuario
```

---

## Seguridad y autenticación

### JWT

- **Algoritmo:** HS256
- **Expiración:** 24 horas (configurable con `app.jwt.expiration-ms`)
- **Claims:** `sub` (email), `rol`
- **Proveedor:** `JwtTokenProvider`
- **Filtro:** `JwtAuthenticationFilter` — intercepta cada request, valida el token y carga el `UserDetails` en el `SecurityContext`

### Header requerido

```
Authorization: Bearer <token>
```

### Endpoints públicos (sin token)

```
POST  /api/v1/auth/register
POST  /api/v1/auth/login
GET   /api/v1/pagos/stripe/config
GET   /swagger-ui/**
GET   /api-docs/**
```

### Bloqueo de cuenta

La entidad `Usuario` tiene campos `intentosFallidos` y `bloqueado`. Tras varios intentos fallidos de login el backend lanza una excepción y bloquea al usuario. Un ADMIN puede desbloquearlo con `PATCH /api/v1/usuarios/{id}/desbloquear`.

### Encriptación de contraseñas

`BCryptPasswordEncoder` — las contraseñas nunca se almacenan en texto plano.

---

## Roles y permisos

El sistema tiene **3 roles**, aplicados via `@PreAuthorize` en cada endpoint:

| Rol | Descripción |
|---|---|
| `ADMIN` | Acceso total. Puede gestionar usuarios, sucursales, catálogos, ver todos los reportes y confirmar pagos |
| `MECANICO` | Puede ver y gestionar órdenes asignadas, registrar horas, ver inventario, confirmar pagos en efectivo/seguro |
| `CLIENTE` | Puede ver sus propios datos, vehículos, órdenes, facturas y citas. Puede aprobar/rechazar presupuestos |

La autorización usa un bean `TallerAuthorization` con métodos de verificación de ownership, como:
- `esClientePropietario(auth, clienteId)` — verifica que el CLIENTE autenticado es dueño del recurso
- `esMecanicoPropietario(auth, mecanicoId)` — verifica que el MECANICO autenticado es el indicado
- `esOrdenAsignadaAlMecanico(auth, ordenId)` — verifica asignación de orden
- `esOrdenDelCliente(auth, ordenId)` — verifica que la orden es del cliente autenticado
- `esFacturaDelCliente(auth, facturaId)` — idem para facturas
- `esVehiculoDelCliente(auth, patente)` — idem para vehículos

---

## Enumeraciones

```java
// Roles del sistema
NombreRol: ADMIN | MECANICO | CLIENTE

// Tipo de orden de trabajo
TipoOrden: EXPRESS | ESTANDAR | GARANTIA | SEGURO

// Estados de una orden de trabajo
EstadoOrden: PENDIENTE | PENDIENTE_APROBACION | EN_PROGRESO | ESPERANDO_PAGO | COMPLETADA | CANCELADA

// Estado de pago de una factura
EstadoPago: PENDIENTE | PENDIENTE_CONFIRMACION | PAGADO | REEMBOLSADO

// Método de pago
MetodoPago: EFECTIVO | STRIPE | SEGURO

// Estado de un servicio del catálogo
EstadoServicio: ACTIVO | INACTIVO

// Estado de una cita
EstadoCita: PROGRAMADA | CONFIRMADA | REPROGRAMADA | COMPLETADA | CANCELADA

// Categoría de repuesto
CategoriaRepuesto: MOTOR | FRENOS | SUSPENSION | TRANSMISION | ELECTRICO |
                   FILTROS | LLANTAS | CARROCERIA | AIRE_ACONDICIONADO |
                   LUBRICANTES | OTROS
```

---

## Manejo de errores

Todas las excepciones son manejadas centralmente por `GlobalExceptionHandler` (`@RestControllerAdvice`). El formato de error es uniforme:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Cliente no encontrado: 99"
}
```

### Excepciones custom

| Clase | HTTP | Cuándo se lanza |
|---|---|---|
| `EntityNotFoundException` | 404 | Recurso no encontrado por ID |
| `BusinessRuleException` | 422 | Violación de regla de negocio (ej: cita duplicada, presupuesto ya aprobado) |
| `DuplicateResourceException` | 409 | Email duplicado, patente duplicada, etc. |
| `StockInsuficienteException` | 409 | No hay stock suficiente del repuesto al crear orden |
| `UnauthorizedException` | 401 | Token inválido o usuario bloqueado |

### Otras respuestas automáticas

| Situación | HTTP |
|---|---|
| `@Valid` falla en DTO | 400 con detalle de campos |
| `AccessDeniedException` (Spring Security) | 403 |
| Token ausente o expirado | 401 |
| Método HTTP no soportado | 405 |
| Content-Type no soportado | 415 |
| Error interno genérico | 500 |

---

## Resumen de endpoints

### AUTH `/api/v1/auth`

| Método | Path | Descripción |
|---|---|---|
| POST | `/register` | Registro de nuevo usuario |
| POST | `/login` | Login, devuelve JWT |

### USUARIOS `/api/v1/usuarios`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/` | ADMIN |
| GET | `/{id}` | ADMIN |
| GET | `/buscar?email=` | ADMIN |
| PUT | `/{id}` | ADMIN |
| DELETE | `/{id}` | ADMIN |
| PATCH | `/{id}/desbloquear` | ADMIN |
| PATCH | `/{id}/cambiar-rol` | ADMIN |
| GET | `/me` | Cualquier rol autenticado |
| PUT | `/me` | Cualquier rol autenticado |
| PATCH | `/me/password` | Cualquier rol autenticado |

### SUCURSALES `/api/v1/sucursales`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/` | Todos |
| GET | `/{id}` | Todos |
| POST | `/` | ADMIN |
| PUT | `/{id}` | ADMIN |
| DELETE | `/{id}` | ADMIN |

### CLIENTES `/api/v1/clientes`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/` | ADMIN, MECANICO |
| GET | `/{id}` | ADMIN, MECANICO, CLIENTE (propio) |
| POST | `/` | ADMIN |
| PUT | `/{id}` | ADMIN, CLIENTE (propio) |
| DELETE | `/{id}` | ADMIN |

### MECÁNICOS `/api/v1/mecanicos`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/` | ADMIN, MECANICO |
| GET | `/{id}` | ADMIN, MECANICO |
| GET | `/sucursal/{sucursalId}` | ADMIN, MECANICO |
| POST | `/` | ADMIN |
| PUT | `/{id}` | ADMIN |
| DELETE | `/{id}` | ADMIN |

### VEHÍCULOS `/api/v1/vehiculos`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/` | ADMIN, MECANICO |
| GET | `/{patente}` | ADMIN, MECANICO, CLIENTE (propio) |
| GET | `/cliente/{clienteId}` | ADMIN, MECANICO, CLIENTE (propio) |
| POST | `/` | ADMIN, CLIENTE |
| PUT | `/{patente}` | ADMIN, CLIENTE (propio) |
| DELETE | `/{patente}` | ADMIN |

### SERVICIOS `/api/v1/servicios`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/` | Todos (solo ACTIVO) |
| GET | `/todos` | ADMIN (activos + inactivos) |
| GET | `/{id}` | Todos |
| POST | `/` | ADMIN |
| PUT | `/{id}` | ADMIN |
| DELETE | `/{id}` | ADMIN (desactiva, no elimina físicamente) |

### PROVEEDORES `/api/v1/proveedores`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/` | ADMIN, MECANICO |
| GET | `/{id}` | ADMIN, MECANICO |
| POST | `/` | ADMIN |
| PUT | `/{id}` | ADMIN |
| DELETE | `/{id}` | ADMIN |

### REPUESTOS `/api/v1/repuestos`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/` | ADMIN, MECANICO |
| GET | `/{id}` | ADMIN, MECANICO |
| POST | `/` | ADMIN |
| PUT | `/{id}` | ADMIN |
| DELETE | `/{id}` | ADMIN |

### INVENTARIO `/api/v1/inventario`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/sucursal/{sucursalId}` | ADMIN, MECANICO |
| GET | `/{id}` | ADMIN, MECANICO |
| POST | `/` | ADMIN |
| PUT | `/{id}` | ADMIN, MECANICO |
| DELETE | `/{id}` | ADMIN |

### ÓRDENES DE TRABAJO `/api/v1/ordenes`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/` | ADMIN |
| GET | `/pendientes?sucursalId=` | ADMIN, MECANICO |
| GET | `/{id}` | ADMIN, MECANICO (asignado), CLIENTE (propio) |
| GET | `/cliente/{clienteId}` | ADMIN, CLIENTE (propio) |
| GET | `/mecanico/{mecanicoId}` | ADMIN, MECANICO (propio) |
| GET | `/vehiculo/{patente}` | ADMIN, MECANICO (asignado), CLIENTE (propio) |
| POST | `/` | ADMIN, MECANICO, CLIENTE |
| PATCH | `/{id}/asignar-mecanico?mecanicoId=` | ADMIN, MECANICO (propio) |
| PATCH | `/{id}/estado?estado=` | ADMIN, MECANICO (asignado) |
| PATCH | `/{id}/presupuesto` | ADMIN, MECANICO (asignado) |
| PATCH | `/{id}/aprobar-presupuesto` | ADMIN, CLIENTE (propio) |
| PATCH | `/{id}/rechazar-presupuesto` | ADMIN, CLIENTE (propio) |
| PATCH | `/{id}/completar` | ADMIN, MECANICO (asignado) |
| DELETE | `/{id}` | ADMIN, MECANICO (asignado) |

### REGISTRO DE HORAS `/api/v1/horas`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/orden/{ordenId}` | ADMIN, MECANICO |
| GET | `/mecanico/{mecanicoId}` | ADMIN, MECANICO (propio) |
| POST | `/` | ADMIN, MECANICO |
| DELETE | `/{id}` | ADMIN, MECANICO |

### FACTURAS `/api/v1/facturas`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/` | ADMIN |
| GET | `/{id}` | ADMIN, CLIENTE (propia) |
| GET | `/orden/{ordenId}` | ADMIN, MECANICO (asignado), CLIENTE (propia) |
| GET | `/cliente/{clienteId}` | ADMIN, CLIENTE (propio) |
| POST | `/pagar` | ADMIN, CLIENTE (propia) |
| PATCH | `/{id}/solicitar-pago-efectivo` | ADMIN, CLIENTE (propia) |
| PATCH | `/{id}/confirmar-pago-efectivo` | ADMIN, MECANICO |
| PATCH | `/{id}/confirmar-pago-seguro` | ADMIN, MECANICO |

### PAGOS STRIPE `/api/v1/pagos/stripe`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/config` | Público (sin token) |
| POST | `/` | ADMIN, CLIENTE |

### CITAS `/api/v1/citas`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/{id}` | ADMIN, MECANICO, CLIENTE (propia) |
| GET | `/cliente/{clienteId}` | ADMIN, CLIENTE (propio) |
| GET | `/mecanico/{mecanicoId}` | ADMIN, MECANICO (propio) |
| GET | `/sucursal/{sucursalId}?fecha=` | ADMIN, MECANICO |
| GET | `/pendientes?sucursalId=` | ADMIN, MECANICO |
| POST | `/` | ADMIN, CLIENTE |
| PATCH | `/{id}/aceptar?mecanicoId=` | ADMIN, MECANICO (propio) |
| PATCH | `/{id}/reprogramar` | ADMIN, MECANICO |
| PATCH | `/{id}/aceptar-reprogramacion` | ADMIN, CLIENTE (propia) |
| PATCH | `/{id}/confirmar` | ADMIN, MECANICO |
| DELETE | `/{id}` | ADMIN, CLIENTE (propia) |

### NOTIFICACIONES `/api/v1/notificaciones`

| Método | Path | Rol mínimo |
|---|---|---|
| GET | `/usuario/{usuarioId}` | Propio usuario / ADMIN |
| GET | `/usuario/{usuarioId}/no-leidas` | Propio usuario / ADMIN |
| GET | `/usuario/{usuarioId}/contador` | Propio usuario / ADMIN |
| PATCH | `/{id}/leer` | Propio usuario / ADMIN |
| PATCH | `/usuario/{usuarioId}/leer-todas` | Propio usuario / ADMIN |

### REPORTES `/api/v1/reportes`

| Método | Path | Descripción |
|---|---|---|
| GET | `/ordenes?desde=&hasta=&sucursalId=` | Órdenes en rango de fechas |
| GET | `/mecanicos/horas` | Horas totales por mecánico |
| GET | `/repuestos/mas-usados` | Repuestos más usados |
| GET | `/ordenes/por-sucursal` | Cantidad de órdenes por sucursal |
| GET | `/resumen` | Resumen global del taller |

---

## Flujos de negocio importantes

### Flujo de orden de trabajo

```
POST /ordenes
   │ estado: PENDIENTE
   ▼
PATCH /{id}/asignar-mecanico   ← ADMIN o mecánico se autoasigna
   │ estado: PENDIENTE → EN_PROGRESO
   ▼
PATCH /{id}/presupuesto        ← mecánico envía presupuesto
   │ estado: PENDIENTE_APROBACION
   ▼
PATCH /{id}/aprobar-presupuesto   ← cliente aprueba
   │        ó
PATCH /{id}/rechazar-presupuesto  ← cliente rechaza → CANCELADA
   │
   ▼
PATCH /{id}/completar          ← genera Factura automáticamente con IVA 13%
   │ estado: ESPERANDO_PAGO → COMPLETADA
   ▼
(flujo de pago en Facturas)
```

### Flujo de pago en efectivo

```
PATCH /facturas/{id}/solicitar-pago-efectivo   ← cliente
   │ estadoPago: PENDIENTE_CONFIRMACION
   ▼
PATCH /facturas/{id}/confirmar-pago-efectivo   ← admin/mecánico
   │ estadoPago: PAGADO
```

### Flujo de pago con Stripe

```
GET  /pagos/stripe/config          ← frontend obtiene publishableKey
   ▼
Stripe.js captura tarjeta → genera token
   ▼
POST /pagos/stripe { facturaId, token }
   │ estadoPago: PAGADO
```

### Flujo de pago de seguro

```
PATCH /facturas/{id}/confirmar-pago-seguro   ← admin/mecánico cuando aseguradora paga
   │ estadoPago: PAGADO
```

### Flujo de citas

```
POST /citas  (mecanicoId opcional)
   │ estado: PROGRAMADA
   ▼
PATCH /{id}/aceptar?mecanicoId=   ← mecánico acepta y queda asignado
   │
   ├── PATCH /{id}/reprogramar    ← mecánico propone nueva fecha
   │      ▼
   │   PATCH /{id}/aceptar-reprogramacion  ← cliente acepta
   │      │ estado: REPROGRAMADA → CONFIRMADA
   │
   ▼
PATCH /{id}/confirmar             ← estado: CONFIRMADA
   ▼
DELETE /{id}                      ← cancelar (204)
```

### Generación de factura

Al llamar `PATCH /ordenes/{id}/completar`:
1. Se suman todos los `precioAplicado` de servicios y repuestos de la orden
2. `subtotal` = suma total
3. `impuestos` = subtotal × 0.13 (IVA 13%)
4. `total` = subtotal + impuestos
5. Se crea la `Factura` con `estadoPago = PENDIENTE`
6. Se envía una `Notificacion` al cliente

---

## CORS

Configurado en `CorsConfig`. Orígenes permitidos:

```
http://localhost:3000    (React CRA)
http://localhost:5173    (Vite)
http://localhost:5174    (Vite alternativo)
https://frontend-pnc-proyecto-the-boys.vercel.app/
     (deploys de preview/producción)
```

Métodos permitidos: `GET, POST, PUT, PATCH, DELETE, OPTIONS`

Headers expuestos: `Authorization`

`credentials: true` habilitado para envío de cookies/headers de auth.
