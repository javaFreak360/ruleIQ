package com.highcourt.ruleIQ.entities;

import java.io.Serializable;

public record Action(ActionTypeEnum type, Object... params) implements Serializable {
}
