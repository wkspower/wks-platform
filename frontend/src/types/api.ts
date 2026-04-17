export interface ApiSuccessEnvelope<T> {
  data: T;
  meta: Record<string, unknown>;
}

export interface ApiErrorBody {
  code: string;
  message: string;
  field?: string | null;
}

export interface ApiErrorEnvelope {
  error: ApiErrorBody;
  meta: Record<string, unknown>;
}
