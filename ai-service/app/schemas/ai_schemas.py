from __future__ import annotations

from typing import Literal
from pydantic import BaseModel, Field


class IssueSuggestion(BaseModel):
    title: str = Field(..., description="Concise issue title")
    description: str = Field(..., description="Detailed description of the issue")
    priority: Literal["LOW", "MEDIUM", "HIGH", "CRITICAL"]
    status: Literal["OPEN"] = "OPEN"


class SuggestIssuesRequest(BaseModel):
    project_name: str = Field(..., min_length=1, max_length=120)
    project_description: str = Field(default="", max_length=10000)
    context: str = Field(default="", max_length=10000, description="Free-form feature context")
    count: int = Field(default=5, ge=1, le=20)


class SuggestIssuesResponse(BaseModel):
    issues: list[IssueSuggestion]
    model: str
    generated_by: str = "devtrack-ai-service"


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
