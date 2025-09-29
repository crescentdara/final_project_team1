import { useState, useEffect } from "react";
import axios from "axios";
import { Form, Button, Alert } from "react-bootstrap";

function MessageSend({ senderId, onMessageSent }) {
    const [receivers, setReceivers] = useState([]);
    const [receiverId, setReceiverId] = useState("");
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const [successMsg, setSuccessMsg] = useState("");
    const [errorMsg, setErrorMsg] = useState("");

    // 조사원 목록 불러오기
    useEffect(() => {
        axios
            .get("/web/api/users/simple")
            .then((res) => setReceivers(res.data))
            .catch((err) => console.error("조사원 목록 불러오기 실패:", err));
    }, []);

    // 메시지 전송
    const handleSend = async (e) => {
        e.preventDefault();
        try {
            await axios.post("/web/api/messages/send", {
                senderId,
                receiverId: receiverId === "ALL" ? null : Number(receiverId),
                title,
                content,
            });

            // 새 메시지 객체를 만들어 부모(MessageTabs)로 전달 → 보낸함 즉시 반영
            const newMessage = {
                messageId: Date.now(),
                senderId,
                receiverId: receiverId === "ALL" ? null : Number(receiverId),
                receiverName:
                    receiverId === "ALL"
                        ? "전체"
                        : receivers.find((r) => r.userId === Number(receiverId))?.name || "",
                title,
                content,
                sentAt: new Date(),
                readFlag: false,
            };

            if (onMessageSent) {
                onMessageSent(newMessage);
            }

            setSuccessMsg("메시지가 성공적으로 전송되었습니다!");
            setErrorMsg("");
            setTitle("");
            setContent("");
            setReceiverId("");
        } catch (err) {
            console.error(err);
            setErrorMsg("메시지 전송에 실패했습니다.");
            setSuccessMsg("");
        }
    };

    return (
        <div className="p-3 border rounded bg-white shadow-sm">
            <h5 className="mb-4" style={{ color: "#6898FF", fontWeight: "bold" }}>
                메시지 보내기
            </h5>

            {successMsg && (
                <Alert variant="success" className="fw-bold">
                    {successMsg}
                </Alert>
            )}
            {errorMsg && (
                <Alert variant="danger" className="fw-bold">
                    {errorMsg}
                </Alert>
            )}

            <Form onSubmit={handleSend}>
                {/* 수신자 선택 */}
                <Form.Group className="mb-3">
                    <Form.Label className="fw-bold">수신자</Form.Label>
                    <Form.Select
                        value={receiverId}
                        onChange={(e) => setReceiverId(e.target.value)}
                        style={{ borderRadius: "8px" }}
                    >
                        <option value="">-- 조사원 선택 --</option>
                        <option value="ALL">전체</option>
                        {receivers.map((r) => (
                            <option key={r.userId} value={r.userId}>
                                {r.name} (ID: {r.userId})
                            </option>
                        ))}
                    </Form.Select>
                </Form.Group>

                {/* 제목 */}
                <Form.Group className="mb-3">
                    <Form.Label className="fw-bold">제목</Form.Label>
                    <Form.Control
                        type="text"
                        placeholder="제목 입력"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                        style={{ borderRadius: "8px" }}
                    />
                </Form.Group>

                {/* 내용 */}
                <Form.Group className="mb-4">
                    <Form.Label className="fw-bold">내용</Form.Label>
                    <Form.Control
                        as="textarea"
                        rows={5}
                        placeholder="메시지 내용을 입력하세요"
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        required
                        style={{ borderRadius: "8px" }}
                    />
                </Form.Group>

                {/* 전송 버튼 */}
                <Button
                    type="submit"
                    className="w-100 fw-bold"
                    style={{
                        backgroundColor: "#6898FF",
                        border: "none",
                        borderRadius: "8px",
                        padding: "12px",
                        boxShadow: "0 4px 10px rgba(104,152,255,0.3)",
                    }}
                >
                    전송
                </Button>
            </Form>
        </div>
    );
}

export default MessageSend;
