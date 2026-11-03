# sportix-notifications

Microservicio propio de Sportix que **entrega por correo** los códigos de verificación
(segundo factor al iniciar sesión — HU-SE-10 — y recuperación de contraseña — HU-SE-09).

Sportixcore **genera y valida** el código; este servicio **solo lo envía**. Reemplaza al
servicio Flask + Gmail API que consumíamos antes, con el **mismo contrato**, así que
Sportixcore casi no cambió (solo se agregó el header `X-API-Key`).

```
Sportixcore ──POST /api/v1/2fa/send + X-API-Key──▶ sportix-notifications ──SMTP──▶ Gmail ──▶ bandeja del usuario
```

## Contrato

`POST /api/v1/2fa/send` — header `X-API-Key: <clave compartida>`

```json
{ "to": "ana@mail.com", "code": "482915", "ttl_minutes": 5, "request_id": "abc", "purpose": "login_2fa" }
```

| Respuesta | Cuándo |
|---|---|
| `200 {"status":"ok","message_id":"...","dry_run":false,"request_id":"abc"}` | Correo entregado a Gmail |
| `400 {"status":"error","message":"Datos inválidos","errors":{...}}` | `to` no es correo, `code` no tiene 6 dígitos, `ttl_minutes` fuera de 1–60, `purpose` desconocido |
| `401 {"status":"error","message":"Falta el header X-API-Key..."}` | Clave ausente o incorrecta |
| `502 {"status":"error","message":"..."}` | Gmail rechazó el envío (contraseña de aplicación mala, sin internet...) |

`purpose`: `login_2fa` (por defecto) o `password_reset`. Cambia el asunto y el texto del correo.

`GET /health` → `{"status":"UP","mode":"smtp"}` (no pide clave).

## Estructura

```
src/main/java/com/sportix/notifications/
├── controller/   VerificationCodeController (POST /api/v1/2fa/send), HealthController
├── dto/          SendCodeRequest (validaciones), SendCodeResponse
├── model/        CodePurpose (login_2fa / password_reset)
├── service/      VerificationCodeSender (orquesta), VerificationEmailTemplate (texto + HTML)
├── mail/         MailGateway (interfaz) ─┬─ SmtpMailGateway   (MAIL_MODE=smtp → Gmail)
│                                         └─ ConsoleMailGateway (MAIL_MODE=console → solo log)
├── security/     ApiKeyFilter (X-API-Key en /api/**)
├── exception/    GlobalExceptionHandler (400 / 502 con JSON)
└── config/       NotificationProperties (lee el .env)
```

El servicio depende de la **interfaz** `MailGateway`, no de Gmail: para cambiar de
proveedor (SendGrid, Outlook...) se agrega otra implementación sin tocar el resto.

## Puesta en marcha paso a paso

### 1. Crear la contraseña de aplicación de Gmail

Gmail no deja usar la contraseña normal desde un programa. Se usa una
**contraseña de aplicación** (16 letras):

1. Entre a la cuenta de Gmail que enviará los correos (ideal: una cuenta del equipo, no personal).
2. Active la **verificación en dos pasos**: https://myaccount.google.com/security
3. Vaya a https://myaccount.google.com/apppasswords
4. Nombre: `Sportix`, botón **Crear**. Copie las 16 letras (sin espacios).

> Si la página dice que no está disponible: falta la verificación en dos pasos, o la cuenta
> es de un dominio (universidad/empresa) que lo tiene bloqueado. Use una cuenta @gmail.com.

### 2. Crear el `.env`

Copie `.env.example` como `.env` (está en `.gitignore`, nunca se sube) y llene:

```properties
PORT=5000
NOTIFICATION_API_KEY=<clave larga aleatoria>
MAIL_MODE=smtp
GMAIL_USERNAME=sportix.equipo@gmail.com
GMAIL_APP_PASSWORD=<las 16 letras>
MAIL_FROM_NAME=Sportix
```

Para generar la clave en PowerShell:

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Max 256 }) -as [byte[]])
```

### 3. Configurar Sportixcore

En el `.env` de **Sportixcore**:

```properties
NOTIFICATION_SERVICE_URL=http://localhost:5000
NOTIFICATION_API_KEY=<LA MISMA clave del paso 2>
```

### 4. Arrancar

```powershell
cd sportix-notifications
.\mvnw.cmd spring-boot:run
```

Debe aparecer `Correo: SMTP smtp.gmail.com:587 como sportix.equipo@gmail.com` y
`Tomcat started on port 5000`. Luego arranque Sportixcore como siempre.

### 5. Probar

```powershell
curl http://localhost:5000/health
```

Después, en el frontend: active el 2FA en **Mi cuenta**, cierre sesión e inicie de nuevo:
el código debe llegar al correo del usuario.

## Probar sin Gmail

Con `MAIL_MODE=console` (valor por defecto) no se envía nada: el correo completo,
con el código, se imprime en la consola del servicio. Sirve para desarrollar sin credenciales.

## Pruebas

```powershell
.\mvnw.cmd test
```

## Problemas frecuentes

| Síntoma | Causa |
|---|---|
| Sportixcore responde 503 "No fue posible enviar el código" | El servicio está apagado, o la URL/puerto no coincide. Revise la consola de Sportixcore. |
| El servicio responde 401 | `NOTIFICATION_API_KEY` distinta en los dos `.env` |
| 502 con `535 Username and Password not accepted` | Se puso la contraseña normal o la de aplicación tiene espacios |
| El correo llega a spam | Normal al principio con cuentas nuevas; marque "No es spam" |
