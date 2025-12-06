export type ServerStatus = "STARTING"|"STOPPING"|"ONLINE"

export type Server = {
    id: string;
    port: number;
    max_players: number;
    online_players?: string[];
    status: ServerStatus;
}

export async function sendCommand(server: string, command: string): Promise<boolean> {
    const response = await fetch("http://localhost:2200/servers/command/" + server, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({command: command})
    });
    if (!response.ok) {
        throw new Error("Failed to send command");
    } else {
        console.log("sent command");
        return true;
    }
}