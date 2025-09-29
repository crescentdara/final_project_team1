import { useState, useEffect } from "react";
import axios from "axios";
import { Table, Form, Button } from "react-bootstrap";

function MessageSent({ senderId, newMessage }) {
    const [messages, setMessages] = useState([]);
    const [receivers, setReceivers] = useState([]);
    const [receiverId, setReceiverId] = useState("");
    const [keyword, setKeyword] = useState("");

    // 조사원 목록 불러오기 (한 번만 실행)
    useEffect(() => {
        axios
            .get("/web/api/users/simple")
            .then((res) => setReceivers(res.data))
            .catch((err) => console.error("조사원 목록 불러오기 실패:", err));
    }, []);

    // 보낸 메시지 기본 조회
    useEffect(() => {
        if (!senderId) return;
        axios
            .get(`/web/api/messages/sent/${senderId}`)
            .then((res) => setMessages(res.data))
            .catch((err) => console.error("보낸 메시지 조회 실패:", err));
    }, [senderId]);

    // 새 메시지가 전송되면 즉시 리스트에 반영
    useEffect(() => {
        if (newMessage) {
            setMessages((prev) => [newMessage, ...prev]);
        }
    }, [newMessage]);

    // 검색 실행
    const handleSearch = () => {
        if (!senderId) return;
        axios
            .get(`/web/api/messages/sent/${senderId}/search`, {
                params: {
                    receiverId: receiverId || undefined,
                    keyword: keyword || undefined,
                },
            })
            .then((res) => setMessages(res.data))
            .catch((err) => console.error("검색 실패:", err));
    };

    return (
        <div className="p-3 border rounded bg-white shadow-sm">
            <h5 className="mb-4" style={{ color: "#6898FF", fontWeight: "bold" }}>
                보낸 메시지함
            </h5>

            {/* 검색 영역 */}
            <div className="d-flex gap-2 mb-4">
                <Form.Select
                    value={receiverId}
                    onChange={(e) => setReceiverId(e.target.value)}
                    style={{ maxWidth: "200px", borderRadius: "8px" }}
                >
                    <option value="">전체 조사원</option>
                    {receivers.map((r) => (
                        <option key={r.userId} value={r.userId}>
                            {r.name} (ID: {r.userId})
                        </option>
                    ))}
                </Form.Select>

                <Form.Control
                    type="text"
                    placeholder="메시지 내용 검색"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    style={{ maxWidth: "250px", borderRadius: "8px" }}
                />

                <Button
                    onClick={handleSearch}
                    className="fw-bold"
                    style={{
                        backgroundColor: "#6898FF",
                        border: "none",
                        borderRadius: "8px",
                        padding: "6px 16px",
                    }}
                >
                    검색
                </Button>
            </div>

            {/* 메시지 리스트 */}
            <Table bordered hover responsive className="align-middle">
                <thead style={{ backgroundColor: "#F5F7FF" }}>
                <tr>
                    <th>수신자</th>
                    <th>제목</th>
                    <th>내용</th>
                    <th>보낸 날짜</th>
                    <th>읽음 여부</th>
                </tr>
                </thead>
                <tbody>
                {messages.length > 0 ? (
                    messages.map((msg) => (
                        <tr key={msg.messageId}>
                            <td>{msg.receiverName || "전체"}</td>
                            <td>{msg.title}</td>
                            <td className="text-truncate" style={{ maxWidth: "200px" }}>
                                {msg.content}
                            </td>
                            <td>
                                {msg.sentAt
                                    ? new Date(msg.sentAt).toLocaleString()
                                    : "-"}
                            </td>
                            <td>
                                {msg.readFlag ? (
                                    <span className="badge bg-success">읽음</span>
                                ) : (
                                    <span className="badge bg-secondary">안읽음</span>
                                )}
                            </td>
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="5" className="text-center text-muted py-4">
                            보낸 메시지가 없습니다.
                        </td>
                    </tr>
                )}
                </tbody>
            </Table>
        </div>
    );
}

export default MessageSent;
