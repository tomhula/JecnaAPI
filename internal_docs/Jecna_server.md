# General

The web is suspected to use [WebToDate](https://webtodate.cz/) CMS, (content management system) because it uses
a `WTDGUID` (WebToDateGUserId) cookie.

## Role

The root (`/`) page is based on your role. The role is selected by the main three buttons on the top of the page. Roles
are: `zajemce`, `student`, `zamestnanec` (unofficial). The role is saved into a `WTDGUID` cookie. These are the values of
the cookie:

| role          | `WTDGUID` cookie value |
|---------------|------------------------|
| `zajemce`     | 0                      |
| `student`     | 10                     |
| `zamestnanec` | 100                    |

### Request

Path: `/user/role?role=student`  
Method: `GET`  
Content-Type: _none_  
Query Parameters:

| Name   | Required | Description                          | Possible values               |
|--------|----------|--------------------------------------|-------------------------------|
| `role` | Yes      | The role being assigned to the user. | zajemce, student, zamestnanec |

### Response

Server responds with the `WTDGUID` cookie, which has an int value. (probably the user id)

## Session

A session is started with a first request to the server. (any request)  
Its end is dictated by the browser. (See [Session](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies#define_the_lifetime_of_a_cookie))

### Session ID cookie

- A session id is indicated by the `JSESSIONID` cookie. (example value: `ucid34sfo72nmfet9aitildblp4psbmm`)
- It has a lifetime (`Exipres` attribute) of ["Session"](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies#define_the_lifetime_of_a_cookie).
- If the cookie is not present yet, any request to the server will create a new session and set the cookie.

# Login

## Request

Path: `/user/login`  
Method: `POST`    
Content-Type: `application/x-www-form-urlencoded`  
Form Parameters:

| Name     | Required | Description                                               | Example values            |
|----------|----------|-----------------------------------------------------------|---------------------------|
| `user`   | Yes      | User's username, also accepts full email.                 | hula, hula@spsejecna.cz   |
| `pass`   | Yes      | User's password.                                          | qwerty1234                |
| `token3` | Yes      | Acquired with the login form. More info [below](#token3). | 51565081                  |
| `submit` | No       | Sent by the button. Has only value "Přihlásit se"         | Přihlásit se (only value) |

### token3

Token3 is a [CSRF Protection](https://laravel.com/docs/9.x/csrf). It is always tied to a session, so each session has
the same token3 for its entire life. It has a numeric value. You can find it as a hidden `input` in the login form. A
selector would be `#loginForm input[name=token3]`.

## Response

- If the user was already logged in, the server will respond with redirect to root (`/`), **even if the credentials are
  incorrect**.
- If the password or username is incorrect, the server will respond (`200 OK`) with a login problem page. (same
  as `/user/login-problem`)
- If token3 is missing or incorrect or a session cookie is not set yet, the user will be redirected (`302 Moved Temporarily`) to `/user/login-problem`.
- If the login is successful, the user will be redirected (`302 Moved Temporarily`) to either:
    - the page, which the login happened
      from. ([Referer header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Referer))
    - or root (`/`) if the [Referer header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Referer) is
      missing.
