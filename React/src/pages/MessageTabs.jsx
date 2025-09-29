import { useState } from "react";
import { Tabs, Tab } from "react-bootstrap";
import MessageSend from "./MessageSend";
import MessageSent from "./MessageSent";

function MessageTabs({ senderId }) {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState(null); // 새 메시지 추적

    return (
        <div className="container mt-4" style={{ fontFamily: "SCD, sans-serif" }}>
            {/* 타이틀 */}
            <div className="d-flex align-items-center mb-4">
                <i
                    className="bi bi-envelope-fill me-2"
                    style={{ color: "#6898FF", fontSize: "1.5rem" }}
                ></i>
                <h2 className="mb-0" style={{ color: "#6898FF", fontWeight: "bold" }}>
                    메시지 관리
                </h2>
            </div>

            {/* 카드 레이아웃 */}
            <div
                className="p-4 shadow bg-white"
                style={{ borderRadius: "16px", minHeight: "600px" }}
            >
                <Tabs
                    defaultActiveKey="send"
                    id="message-tabs"
                    className="mb-3 fw-bold"
                    fill
                >
                    {/* 메시지 보내기 */}
                    <Tab eventKey="send" title="메시지 보내기">
                        <div className="p-3">
                            <MessageSend
                                senderId={senderId}
                                onMessageSent={(msg) => {
                                    setMessages((prev) => [msg, ...prev]);
                                    setNewMessage(msg); // 보낸 메시지함에 즉시 반영
                                }}
                            />
                        </div>
                    </Tab>

                    {/* 보낸 메시지함 */}
                    <Tab eventKey="sent" title="보낸 메시지함">
                        <div className="p-3">
                            <MessageSent
                                senderId={senderId}
                                messages={messages}
                                setMessages={setMessages}
                                newMessage={newMessage} // 새로 보낸 메시지 바로 적용
                            />
                        </div>
                    </Tab>
                </Tabs>
            </div>
        </div>
    );
}

export default MessageTabs;
