DELETE FROM users_problem;
DELETE FROM users;
DELETE FROM problem;

INSERT INTO problem (problem_id, problem_level, problem_url, problem_title) VALUES
  (1001, 5,  'https://www.acmicpc.net/problem/1001', '문제 1001'),
  (1002, 10, 'https://www.acmicpc.net/problem/1002', '문제 1002'),
  (1003, 15, 'https://www.acmicpc.net/problem/1003', '문제 1003'),
  (1004, 20, 'https://www.acmicpc.net/problem/1004', '문제 1004');

INSERT INTO users (
  user_id, slack_id, baekjoon_id, username, boj_tier,
  team_name, user_role, total_solved_count, is_alert_agreed, is_deleted,
  created_at, updated_at
) VALUES
  (1, 'slack_u1', 'boj_u1', '백엔드유저', 15, 'BACKEND_FACE', 'USER', 0, TRUE, FALSE, '2025-12-11 00:00:00', '2025-12-11 00:00:00'),
  (2, 'slack_u2', 'boj_u2', '프론트유저', 10, 'FRONTEND_FACE', 'USER', 0, TRUE, FALSE, '2025-12-11 00:00:00', '2025-12-11 00:00:00'),
  (3, 'slack_u3', 'boj_u3', '비대면유저', 20, 'FRONTEND_NON_FACE', 'USER', 0, TRUE, FALSE, '2025-12-11 00:00:00', '2025-12-11 00:00:00');

INSERT INTO users_problem (problem_id, user_id, is_solved, solved_time) VALUES
  (1002, 1, TRUE, '2025-12-11 13:10:00'),
  (1003, 1, TRUE, '2025-12-11 13:40:00'),
  (1001, 2, TRUE, '2025-12-11 13:20:00'),
  (1002, 2, TRUE, '2025-12-11 13:50:00'),
  (1004, 2, TRUE, '2025-12-11 14:10:00'),
  (1003, 3, TRUE, '2025-12-11 13:05:00');