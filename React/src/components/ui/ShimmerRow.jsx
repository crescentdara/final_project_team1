export default function ShimmerRow({ height = 18, className = "" }) {
  return (
      <div
          className={`w-100 rounded ${className}`}
          style={{
            height,
            background:
                "linear-gradient(90deg, #eee 25%, #f5f5f5 37%, #eee 63%)",
            backgroundSize: "400% 100%",
            animation: "shimmer 1.4s ease infinite",
          }}
      />
  );
}

// 전역 CSS에 아래 keyframes 추가(없으면 index.css에 추가)
// @keyframes shimmer {
//   0% { background-position: 100% 0; }
//   100% { background-position: 0 0; }
// }
