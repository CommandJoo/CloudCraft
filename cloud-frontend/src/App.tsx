import './App.css'
import ServerList from "./components/ServerList.tsx";
import Console from "./components/Console.tsx";
import {useCallback, useEffect, useState} from "react";
import type {Server} from "./types.ts";
import Data from "./components/Data.tsx";

function App() {
    const [servers, setServers] = useState<Server[]>([])
    const [selected, setSelected] = useState("");
    const dark = true;

    const loadServers = useCallback(() => {
        async function load() {
            const response = await fetch("http://localhost:2200/servers");
            if (!response.ok) {
                throw new Error("Servers not found!.");
            }
            const servers = await response.json() as {servers: Server[]};
            setServers(servers.servers);
        }
        load().then();
    }, []);

    useEffect(() => {
        const inter = setInterval(() => {
            loadServers();
        }, 1000);
        return () => {
            clearInterval(inter);
        }
    }, [loadServers]);

    function exists(id: string): boolean {
        return servers.filter(server => server.id===id).length > 0;
    }

    return (
        <div id={"app"} className={dark ? "dark" : "light"}>
            <div id={"top"}>
                <ServerList servers={servers} loadServers={loadServers} selected={selected} setSelected={setSelected}/>
                <Data server={servers.filter(server => server.id===selected).pop()}/>
            </div>
            <div id={"bottom"}>
                <Console selected={exists(selected) ? selected : ""}/>
            </div>
        </div>
    )
}

export default App
