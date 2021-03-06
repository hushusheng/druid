/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.ast.statement;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public class SQLUnique extends SQLConstraintImpl implements SQLUniqueConstraint, SQLTableElement {

    protected final List<SQLSelectOrderByItem> columns = new ArrayList<SQLSelectOrderByItem>();

    public SQLUnique(){

    }

    public List<SQLSelectOrderByItem> getColumns() {
        return columns;
    }
    
    public void addColumn(SQLExpr column) {
        if (column != null) {
            column.setParent(this);
        }
        this.columns.add(new SQLSelectOrderByItem(column));
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.getName());
            acceptChild(visitor, this.getColumns());
        }
        visitor.endVisit(this);
    }

    public boolean containsColumn(String column) {
        for (SQLSelectOrderByItem item : columns) {
            SQLExpr expr = item.getExpr();
            if (expr instanceof SQLIdentifierExpr) {
                if (SQLUtils.nameEquals(((SQLIdentifierExpr) expr).getName(), column)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void cloneTo(SQLUnique x) {
        super.cloneTo(x);

        for (SQLSelectOrderByItem column : columns) {
            SQLSelectOrderByItem column2 = column.clone();
            column2.setParent(x);
            x.columns.add(column2);
        }
    }

    public SQLUnique clone() {
        SQLUnique x = new SQLUnique();
        cloneTo(x);
        return x;
    }

    public void simplify() {
        super.simplify();

        for (SQLSelectOrderByItem item : columns) {
            SQLExpr column = item.getExpr();
            if (column instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr identExpr = (SQLIdentifierExpr) column;
                String columnName = identExpr.getName();
                columnName = SQLUtils.normalize(columnName, dbType);
                identExpr.setName(columnName);
            }
        }
    }
}
