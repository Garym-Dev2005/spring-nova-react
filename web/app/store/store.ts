import { configureStore } from "@reduxjs/toolkit";
import { counterSlice } from "./counterSlice";

// A factory rather than a module singleton, so tests (and the build-time
// prerender of index.html) each get a fresh store.
export function makeStore() {
  return configureStore({
    reducer: {
      [counterSlice.reducerPath]: counterSlice.reducer,
    },
  });
}

export type AppStore = ReturnType<typeof makeStore>;
export type RootState = ReturnType<AppStore["getState"]>;
export type AppDispatch = AppStore["dispatch"];
