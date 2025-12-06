import "./Data.css"
import {sendCommand, type Server} from "../types.ts";

type DataProps = {
    server?: Server
}

export default function Data(props: DataProps) {
    return <div id={"data"}>
        <div id={"list"}>
            {props.server && props.server.online_players && props.server.online_players.map((p) => {
                return <div key={p} id={"player"}>
                    <p>{p}</p>
                    <button onClick={() => {
                        if (props.server) {
                            sendCommand(props.server.id, "kick " + p);
                        }
                    }}>kick
                    </button>
                </div>;
            })}
        </div>
    </div>
}