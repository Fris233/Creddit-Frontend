package com.crdt;

import java.util.ArrayList;
import java.util.Map;

public record CommentFeed(
        Comment[] parents,
        Map<Integer, Comment[]> lv2,
        Map<Integer, Comment[]> lv3,
        Map<Integer, Integer> votes
) {}
