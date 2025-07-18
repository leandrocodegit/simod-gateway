package br.sincroled.gateway;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RouterValidator {


    public static Map<String, EndPonitValid> routes = Map.of(
            "/processo", new EndPonitValid(List.of("view-events","manage-users","view-users"), true,  Collections.EMPTY_LIST),
            "/processo/group", new EndPonitValid(List.of("view-events","manage-users","view-users"), true,  Collections.EMPTY_LIST)

            );
}
