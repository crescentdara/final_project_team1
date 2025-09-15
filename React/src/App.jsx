import React, { useEffect, useState } from "react";
import { getTest } from "./api";

function App() {
    const [msg, setMsg] = useState("");

    useEffect(() => {
        getTest().then(setMsg);
    }, []);

    return <h1>{msg}</h1>;
}

export default App;
