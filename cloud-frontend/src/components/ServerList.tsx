import "./ServerList.css"
import type {Server} from "../types.ts";
import {useCallback, useEffect, useState} from "react";

type ServerListProps = {
    servers: Server[]
    loadServers: () => void;
    selected: string
    setSelected: (id: string) => void
}

function ServerEntry({server, setSelected, selected}: {
    key: string;
    server: Server,
    setSelected: (id: string) => void,
    selected: string
}) {
    async function stopServer() {
        const response = await fetch("http://localhost:2200/servers/stop/" + server.id, {
            method: "POST",
        })
        if (!response.ok) {
            throw Error("Could not stop the server.");
        } else {
            console.log("stopped server");
        }
    }

    return <div id={"server"} className={selected === server.id ? "selected" : ""} onClick={() => {
        setSelected(server.id)
    }}>
        <div id={"left"}>
            <div id={"status"}
                 className={server.status === "STARTING" ? "yellow" : server.status === "ONLINE" ? "green" : server.status === "STOPPING" ? "red" : "none"}>
                <span id={"tooltip"}>{server.status.toLocaleLowerCase()}</span>
            </div>
            <h1>{server.id}</h1>
        </div>
        <div id={"right"}>
            <p>{(server.online_players ? server.online_players.length : "0") + "/" + server.max_players}</p>
            <hr/>
            <button onClick={() => {
                stopServer();
            }}>Stop
            </button>
        </div>
    </div>
}

export default function ServerList(props: ServerListProps) {
    const [templates, setTemplates] = useState<string[]>([]);

    const [maxPlayers, setMaxPlayers] = useState<number>(10);
    const [template, setTemplate] = useState<string>("");

    useEffect(() => {
        async function load() {
            props.loadServers()
        }

        load().then()
    }, [props]);

    async function startServer() {
        const response= await fetch("http://localhost:2200/servers/start", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({max_players: maxPlayers, template: template}),
        })
        if(!response.ok) {
            throw Error("Could not start the server.");
        }else {
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                const json = await response.json() as { success: true };
                if(!json.success) {
                    throw new Error("Could not start server.");
                }else {
                    console.log("started server");
                }
            }
        }
    }

    const loadTemplates = useCallback(async () => {
        const response = await fetch("http://localhost:2200/templates", {
            method: "GET",
        })
        if (!response.ok) {
            throw Error("Could not load templates.");
        } else {
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                const json = await response.json() as { success: true, templates: string[] };
                if(!json.success) {
                    throw new Error("Could not load templates.");
                }else {
                    setTemplates(json.templates)
                }
            }
        }
    }, []);

    useEffect(() => {
        async function load() {
            await loadTemplates().then(() => {
                setTemplate(templates[0])
            });
        }
        load().then();
    }, [loadTemplates, templates]);

    return <div id={"server-list"}>
        {props.servers && props.servers.map((server) => {
            return <ServerEntry key={server.id} server={server} selected={props.selected} setSelected={props.setSelected}/>
        })}
        <div id={"new-server"}>
            <input type="text"
                   value={maxPlayers}
                   onChange={(e) => {
                       // Remove non-digits
                       const cleaned = e.target.value.replace(/\D/g, "");
                       setMaxPlayers(cleaned === "" ? 0 : Number(cleaned));
                   }}
                   onKeyDown={(e) => {
                       // Block unwanted keys: letters, symbols, etc.
                       if (
                           e.key.length === 1 && !/[0-9]/.test(e.key) || // letters & symbols
                           e.key === "e" ||
                           e.key === "E" ||
                           e.key === "-" ||
                           e.key === "+"
                       ) {
                           e.preventDefault();
                       }
                   }} id={"max-players"}/>
            <select id={"template"} onChange={(e) => {
                setTemplate(e.target.value);
            }} onClick={loadTemplates}>
                {templates.map((template) => {
                    return <option key={template} value={template}>{template}</option>
                })}
            </select>
            <button id={"start"} onClick={startServer}>
                <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                    <path d="m 2 2.5 v 11 c 0 1.5 1.269531 1.492188 1.269531 1.492188 h 0.128907 c 0.246093 0.003906 0.488281 -0.050782 0.699218 -0.171876 l 9.796875 -5.597656 c 0.433594 -0.242187 0.65625 -0.734375 0.65625 -1.226562 c 0 -0.492188 -0.222656 -0.984375 -0.65625 -1.222656 l -9.796875 -5.597657 c -0.210937 -0.121093 -0.453125 -0.175781 -0.699218 -0.175781 h -0.128907 s -1.269531 0 -1.269531 1.5 z m 0 0" fill="#55FF55"/>
                </svg>
            </button>
        </div>
    </div>
}