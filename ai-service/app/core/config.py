from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    jwt_secret: str = ""
    jwt_algorithm: str = "HS256"
    openai_api_key: str = ""
    openai_model: str = "gpt-4o-mini"
    log_level: str = "INFO"

    class Config:
        env_file = ".env"


settings = Settings()
