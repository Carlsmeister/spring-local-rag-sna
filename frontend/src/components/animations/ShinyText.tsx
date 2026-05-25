
interface ShinyTextProps {
  text: string;
  disabled?: boolean;
  speed?: number;
  className?: string;
}

export default function ShinyText({ text, disabled = false, speed = 6, className = '' }: ShinyTextProps) {
  const animationDuration = `${speed}s`;

  return (
    <span
      className={`inline-block text-transparent bg-clip-text bg-gradient-to-r from-slate-100 via-indigo-400 to-slate-100 bg-[length:200%_auto] ${
        disabled ? '' : 'animate-shiny-text'
      } ${className}`}
      style={{ animationDuration }}
    >
      {text}
    </span>
  );
}
