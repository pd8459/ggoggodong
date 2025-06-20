package com.example.ggoggodong.model;

/**
 * 한 Stroke(선)에 대한 좌표‧색상‧두께 정보를 담는 순수 데이터 객체(Pojo)이다.
 * Firebase Realtime Database 직렬화를 위해
 * ① public 필드 또는 public getter/setter
 * ② 매개변수 없는 기본 생성자
 * 가 반드시 필요하다.
 */
public class Line {

    // ── 좌표 ─────────────────────────────────────────────
    public float startX;
    public float startY;
    public float endX;
    public float endY;

    // ── 스타일 ───────────────────────────────────────────
    public String color;   // 예: "#FF0000"
    public int    width;   // 선 두께(px)

    /** Firebase가 객체를 역직렬화할 때 사용되는 빈 생성자이다. */
    public Line() { }

    /** 모든 필드를 초기화하는 생성자이다. */
    public Line(float startX, float startY,
                float endX,   float endY,
                String color, int    width) {
        this.startX = startX;
        this.startY = startY;
        this.endX   = endX;
        this.endY   = endY;
        this.color  = color;
        this.width  = width;
    }
}
