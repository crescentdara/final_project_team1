import { useState, useEffect } from "react";
import axios from "axios";
import { Table, Form, Button } from "react-bootstrap";

function MessageSent({ senderId, newMessage }) {
    console.log("✅ MessageSent senderId:", senderId); // 디버깅

    const [messages, setMessages] = useState([]);
    const [receivers, setReceivers] = useState([]);
    const [receiverId, setReceiverId] = useState("");
    const [keyword, setKeyword] = useState("");

    // 📌 조사원 목록 불러오기 (한 번만 실행)
    useEffect(() => {
        axios
            .get("/web/api/users/simple")
            .then((res) => setReceivers(res.data))
            .catch((err) => console.error("조사원 목록 불러오기 실패:", err));
    }, []);

    // 📌 보낸 메시지 기본 조회
    useEffect(() => {
        if (!senderId) return;
        axios
            .get(`/web/api/messages/sent/${senderId}`)
            .then((res) => setMessages(res.data))
            .catch((err) => console.error("보낸 메시지 조회 실패:", err));
    }, [senderId]);

    // 📌 새 메시지가 전송되면 즉시 리스트에 반영
    useEffect(() => {
        if (newMessage) {
            setMessages((prev) => [newMessage, ...prev]);
        }
    }, [newMessage]);

    // 📌 검색 실행
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
        <div>
            <h4 className="p-3">보낸 메시지함</h4>

            {/* 🔍 검색영역 */}
            <div className="d-flex flex-wrap gap-2 align-items-center justify-content-end mb-3">
                <select
                    className="form-select"
                    style={{ maxWidth: 120, height: 40 }}
                    value={receiverId}
                    onChange={(e) => setReceiverId(e.target.value)}
                >
                    <option value="all">전체</option>
                    <option value="name">이름</option>
                    <option value="username">아이디</option>
                    <option value="empNo">사번</option>
                </select>

                <div className="input-group input-group-sm" style={{ maxWidth: 300, height: 40 }}>
                    <input
                        type="text"
                        className="form-control"
                        placeholder="조사원 검색"
                        value={keyword}
                        onChange={(e) => setKeyword(e.target.value)}
                    />
                    <button className="btn btn-outline-secondary" onClick={handleSearch}>검색</button>
                </div>
            </div>

            {/* 메시지 리스트 */}
            <Table
                striped
                bordered
                hover
                className="align-middle"
                style={{ tableLayout: "fixed", width: "100%"}}
            >
                {/* 각 컬럼 폭 지정(원하는 값으로 조절 가능) */}
                <colgroup>
                    <col style={{ width: "75px" }}/>  {/* 수신자 */}
                    <col style={{ width: "100px" }} />  {/* 제목 */}
                    <col style={{ width: "100px" }}/>
                    <col style={{ width: "90px" }} />  {/* 보낸 날짜 */}
                    <col style={{ width: "90px" }} />  {/* 읽음 여부 */}
                </colgroup>

                <thead>
                <tr className="text-center" >
                    <th>수신자</th>
                    <th>제목</th>
                    <th>내용</th>
                    <th>보낸 날짜</th>
                    <th>읽음 여부</th>
                </tr>
                </thead>

                <tbody className="text-center">
                {messages.length > 0 ? (
                    messages.map((msg) => (
                        <tr key={msg.messageId}>
                            <td>{msg.receiverName || "전체"}</td>

                            {/* 제목: 줄바꿈 없이 가로 스크롤로 모두 보이기 */}
                            <td>
                                <div
                                    className="text-truncate"
                                    style={{ maxWidth: "100%" }}
                                    title={msg.title} // 호버 시 전체 툴팁
                                >
                                    {msg.title}
                                </div>
                            </td>

                            {/* 내용: 넘치면 말줄임표 */}
                            <td>
                                <div
                                    className="text-truncate"
                                    style={{ maxWidth: "100%" }}
                                    title={msg.content} // 호버 시 전체 내용
                                >
                                    {msg.content}
                                </div>
                            </td>

                            <td className="text-truncate">
                                {msg.sentAt ? new Date(msg.sentAt).toLocaleDateString() : "-"}
                            </td>
                            <td>{msg.readFlag ? "읽음" : "안읽음"}</td>
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="5" className="text-center">
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
