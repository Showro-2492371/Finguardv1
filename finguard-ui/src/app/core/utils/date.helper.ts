export function parseBackendDate(value: string | number[] | null | undefined): Date | null {
  if (!value) return null;
  if (Array.isArray(value)) {
    const [y, mo, d, h = 0, mi = 0, s = 0] = value as number[];
    return new Date(y, mo - 1, d, h, mi, s);
  }
  return new Date(value as string);
}

