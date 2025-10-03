// MessageTabs.jsx
import { useState } from "react";
import { Tabs, Tab } from "react-bootstrap";
import MessageSend from "./MessageSend";
import MessageSent from "./MessageSent";

function MessageTabs({ senderId }) {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState(null); // 새 메시지 추적

    return (
        <div
            className="container-fluid shadow-sm rounded-3"
            style={{ backgroundColor: "#fff" }}
        >
            <h3
                className="fw-bold mb-4"
                style={{ borderLeft: "4px solid #6898FF", paddingLeft: "12px" }}
            >
                메시지 관리
            </h3>

            <Tabs
                defaultActiveKey="send"
                id="message-tabs"
                className="mb-3 custom-tabs"
                fill
            >
                {/* 메시지 보내기 */}
                <Tab eventKey="send" title="메시지 보내기">
                    <MessageSend
                        senderId={senderId}
                        onMessageSent={(msg) => {
                            setMessages((prev) => [msg, ...prev]);
                            setNewMessage(msg); // 보낸 메시지함에 즉시 반영
                        }}
                    />
                </Tab>

                {/* 보낸 메시지함 */}
                <Tab eventKey="sent" title="보낸 메시지함">
                    <MessageSent
                        senderId={senderId}
                        messages={messages}
                        setMessages={setMessages}
                        newMessage={newMessage} // 새로 보낸 메시지 바로 적용
                    />
                </Tab>
            </Tabs>
        </div>
    );
}

export default MessageTabs;
