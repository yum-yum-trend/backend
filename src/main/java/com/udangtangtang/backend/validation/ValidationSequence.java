package com.udangtangtang.backend.validation;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;
import com.udangtangtang.backend.validation.ValidationGroups.NotEmptyGroup;
import com.udangtangtang.backend.validation.ValidationGroups.PatternCheckGroup;

@GroupSequence({Default.class, NotEmptyGroup.class, PatternCheckGroup.class })
public interface ValidationSequence {
}