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
