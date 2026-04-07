package com.gearrent.service;

import com.gearrent.dao.MembershipConfigDAO;
import com.gearrent.entity.MembershipConfig;
import java.sql.SQLException;
import java.util.List;

public class MembershipService {
    private final MembershipConfigDAO dao = new MembershipConfigDAO();

    public List<MembershipConfig> getAll()         throws SQLException { return dao.findAll(); }
    public MembershipConfig getByLevel(String l)   throws SQLException { return dao.findByLevel(l); }

    public void update(MembershipConfig mc) throws Exception {
        if (mc.getDiscountPercentage() < 0 || mc.getDiscountPercentage() > 100)
            throw new Exception("Discount must be between 0 and 100.");
        if (mc.getDepositLimit() <= 0)
            throw new Exception("Deposit limit must be > 0.");
        dao.update(mc);
    }
}
