package com.example.daftar.backend.localDb

const val PAGE_BALANCES_SQL = """
        SELECT p.id, p.name, p.number,
            COALESCE(SUM(CASE WHEN t.isPayed THEN -CAST(t.credit AS INTEGER) ELSE t.credit END), 0) AS credit
        FROM Pages p LEFT JOIN transactions t ON p.id = t.pageId
        GROUP BY p.id ORDER BY p.id DESC
    """
