package org.deri.cqels.engine;

import com.hp.hpl.jena.query.Query;

/**
 * This class acts as a router standing in the root of the tree if the query is
 * a construct-type
 *
 * @author	Danh Le Phuoc
 * @author Chan Le Van
 * @organization DERI Galway, NUIG, Ireland www.deri.ie
 * @author Michael Jacoby
 * @organization Karlsruhe Institute of Technology, Germany www.kit.edu
 * @email danh.lephuoc@deri.org
 * @email chan.levan@deri.org
 * @email michael.jacoby@student.kit.edu
 */
public class ContinuousConstruct extends ContinuousQuery<ConstructListener> {

    public ContinuousConstruct(ExecContext context, Query query, OpRouter subRouter) {
        super(context, query, subRouter);
        isConstruct = true;
    }

    @Override
    public void register(ConstructListener lit) {
        lit.setTemplate(query.getConstructTemplate());
        listeners.add(lit);
    }
}
