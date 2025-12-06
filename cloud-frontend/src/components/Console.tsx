import "./Console.css"
import {useCallback, useEffect, useRef, useState} from "react";
import {sendCommand} from "../types.ts";
import AnsiToHtml from "ansi-to-html";

type ConsoleProps = {
    selected: string
}

export default function Console(props: ConsoleProps) {
    const [log, setLog] = useState<string[]>();
    const prevSelected = useRef<string>("");
    const inputRef = useRef<HTMLInputElement>(null);
    const logEndRef = useRef<HTMLDivElement>(null);

    const requestLog = useCallback(() => {
        async function load() {
            if (!props.selected) return;
            const response = await fetch("http://localhost:2200/servers/log/" + props.selected, {
                method: "GET"
            });
            if (!response.ok) {
                throw new Error("Failed to read log");
            } else {
                const contentType = response.headers.get("content-type");
                if(contentType && contentType.includes("application/json") && (("success" in await response.json() && !(await response.json() as {success: boolean})))) {
                    console.log(response.json())
                    setLog([""])
                    return;
                }
                const raw = await response.text();
                setLog(raw.split("\n").map((line) => new AnsiToHtml().toHtml(line)));
            }
        }
        load().then();
    }, [props.selected]);

    useEffect(() => {
        const logContainer = logEndRef.current?.parentElement; // the div with id="log"
        if (!logContainer) return;

        const isAtBottom = logContainer.scrollHeight - logContainer.scrollTop <= logContainer.clientHeight + 80;

        if (isAtBottom) {
            logEndRef.current?.scrollIntoView({ behavior: "smooth" });
        }
    }, [log]);
    useEffect(() => {
        const logContainer = logEndRef.current?.parentElement; // the div with id="log"
        if (!logContainer) return;

        const isNew = prevSelected.current !== props.selected;
        prevSelected.current = props.selected;

        if (isNew) {
            logEndRef.current?.scrollIntoView({ behavior: "smooth" });
        }

        const inter = setInterval(() => {
            requestLog();
        }, 1000)
        return () => {clearInterval(inter);}
    }, [prevSelected, props.selected, requestLog]);

    async function send() {
        if (!inputRef.current) return;
        if (!props.selected) return;
        const target = inputRef.current.value;

        if(await sendCommand(props.selected, target)) {
            inputRef.current.value = "";
            setTimeout(requestLog, 50);
        }
    }

    useEffect(() => {
        requestLog();
    }, [requestLog]);

    return <div id={"console"}>
        <div id={"log"}>
            {log?.map((item,i) => {
                return <p key={i} dangerouslySetInnerHTML={{ __html: item }}></p>;
            })}
            <div ref={logEndRef}></div>
        </div>
        <div id={"lower"}>
            <input disabled={props.selected==""} ref={inputRef} type={"text"} autoFocus={true} onKeyDown={(e) => {
                if (e.key === "Enter") {
                    send();
                }
            }}/>
            <button disabled={props.selected==""} onClick={send}>Send</button>
        </div>
    </div>
}