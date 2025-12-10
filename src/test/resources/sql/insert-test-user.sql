INSERT INTO users (
    user_id, slack_id, baekjoon_id, username, boj_tier,
    team_name, user_role, total_solved_count, is_alert_agreed, is_deleted,
    created_at, updated_at
) VALUES (
             1L,'slack12345', 'dlrbehd120', '이규동', 13,
             'BACKEND_FACE', 'USER', 0, true, false,
             CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
         );