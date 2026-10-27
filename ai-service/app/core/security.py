from __future__ import annotations

import base64
from typing import Annotated

from fastapi import Depends, Header, HTTPException, status
from jose import JWTError, jwt

from app.core.config import settings


def _decode_token(token: str) -> dict:
    try:
        key = base64.b64decode(settings.jwt_secret, validate=True)
        if len(key) < 32:
            raise ValueError("JWT signing key is too short")
        return jwt.decode(
            token,
            key,
            algorithms=[settings.jwt_algorithm],
        )
    except (JWTError, ValueError) as exc:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
        ) from exc


async def get_current_user(
    authorization: Annotated[str | None, Header()] = None,
) -> dict:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing bearer token",
        )
    token = authorization.removeprefix("Bearer ")
    payload = _decode_token(token)
    return payload


CurrentUser = Annotated[dict, Depends(get_current_user)]
