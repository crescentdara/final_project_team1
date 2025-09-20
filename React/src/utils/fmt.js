export function fmtDate(iso) {
  if (!iso) return "-";
  try {
    const d = new Date(iso);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const hh = String(d.getHours()).padStart(2, "0");
    const mm = String(d.getMinutes()).padStart(2, "0");
    return `${y}-${m}-${day} ${hh}:${mm}`;
  } catch {
    return iso;
  }
}

export function fmtStatus(status) {
  // DB가 1/0 이면:
  if (status === 1 || status === "1") return "활성";
  if (status === 0 || status === "0") return "비활성";
  // 그 외 문자열이면 그대로 노출
  return String(status ?? "-");
}
