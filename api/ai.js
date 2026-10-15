module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method Not Allowed' });
  }

  try {
    const { query, context } = req.body || {};
    const apiKey = process.env.GEMINI_API_KEY;

    if (!apiKey) {
      return res.status(200).json({ 
        success: true, 
        answer: 'Vui lòng cấu hình biến môi trường GEMINI_API_KEY trên Vercel Project Settings để kích hoạt AI Tutor.' 
      });
    }

    let prompt = '';
    if (context && context.trim()) {
      prompt = `Bạn là chuyên gia ngôn ngữ tiếng Anh học thuật.\nNgữ cảnh bài đọc: "${context.trim()}"\nNgười học thắc mắc: "${(query || '').trim()}"\nHãy phân tích chi tiết, dễ hiểu bằng tiếng Việt:\n1. Ý nghĩa chính xác trong ngữ cảnh trên.\n2. Cấu trúc ngữ pháp hoặc từ loại liên quan.\n3. 2 ví dụ đặt câu ứng dụng thực tế theo chuẩn CEFR.`;
    } else {
      prompt = `Bạn là Cố vấn Học thuật Tiếng Anh (Academic English Tutor & Advisor) thông minh và chu đáo.\nNgười học vừa nhắn: "${(query || '').trim()}"\nHãy phản hồi một cách tự nhiên, thông minh, đúng trọng tâm bằng tiếng Việt (hoặc tiếng Anh nếu người học hỏi bằng tiếng Anh):\n- Nếu người học chào hỏi, hãy chào lại thân thiện và hướng dẫn ngắn gọn các cách bạn có thể hỗ trợ.\n- Nếu người học yêu cầu viết bài luận, hãy viết bài mẫu chuẩn CEFR C1/C2 kèm bản dịch và từ vựng hay.\n- Nếu người học hỏi ngữ pháp hoặc từ vựng, hãy giải thích rõ ràng kèm ví dụ sinh động.`;
    }

    const apiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=${apiKey}`;
    const apiRes = await fetch(apiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }]
      })
    });

    const json = await apiRes.json();
    const answer = json?.candidates?.[0]?.content?.parts?.[0]?.text;

    if (answer) {
      return res.status(200).json({ success: true, answer: answer.trim() });
    }
    return res.status(200).json({ success: true, answer: 'Cố vấn AI đang xử lý yêu cầu, vui lòng thử lại sau giây lát.' });
  } catch (err) {
    return res.status(500).json({ success: false, error: err.message });
  }
};
