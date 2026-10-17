export function LoadingState({ label = 'Cargando...' }: { label?: string }) {
  return <div className="state state-loading">{label}</div>;
}

export function ErrorState({ message }: { message: string }) {
  return <div className="state state-error">⚠ {message}</div>;
}

export function EmptyState({ label }: { label: string }) {
  return <div className="state state-empty">{label}</div>;
}
