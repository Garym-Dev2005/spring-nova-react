import type { Route } from "./+types/home";
import { useAppDispatch, useAppSelector } from "~/store/hooks";
import { decrement, increment, selectCount } from "~/store/counterSlice";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Spring Nova" },
    { name: "description", content: "Spring Nova web app" },
  ];
}

export default function Home() {
  const count = useAppSelector(selectCount);
  const dispatch = useAppDispatch();

  return (
    <main className="container mx-auto p-8 space-y-4">
      <h1 className="text-2xl font-semibold">Spring Nova</h1>
      <p>React Router v7 (framework mode) with Redux Toolkit.</p>
      <div className="flex items-center gap-4">
        <button
          type="button"
          className="rounded border px-3 py-1"
          onClick={() => dispatch(decrement())}
        >
          −
        </button>
        <span data-testid="count">{count}</span>
        <button
          type="button"
          className="rounded border px-3 py-1"
          onClick={() => dispatch(increment())}
        >
          +
        </button>
      </div>
    </main>
  );
}
